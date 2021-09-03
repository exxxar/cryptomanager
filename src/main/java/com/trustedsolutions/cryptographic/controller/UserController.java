package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.core.cryptolib.forms.CompanyForm;
import com.core.cryptolib.forms.UserRoleForm;
import com.core.cryptolib.forms.UserSelfUpdateForm;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.TrustedDeviceRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.storage.FileSystemStorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class UserController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    TrustedDeviceRepository trustedDeviceRepository;

    @Autowired
    FileSystemStorageService fileStorageService;

    @GetMapping("/user/me")
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "/user/me",
            method = RequestMethod.PUT,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> selfUpdate(@CurrentUser UserPrincipal userPrincipal, @RequestBody @Valid UserSelfUpdateForm userSelfUpdateForm) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (userSelfUpdateForm.getEmail() != null) {
            user.setEmail(userSelfUpdateForm.getEmail());
        }

        if (userSelfUpdateForm.getImageUrl() != null) {
            user.setImageUrl(userSelfUpdateForm.getImageUrl());
        }

        if (userSelfUpdateForm.getName() != null) {
            user.setName(userSelfUpdateForm.getName());
        }

        user = userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/user/me/avatar",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3"})
    @ResponseBody
    public ResponseEntity<Object> uploadAvatar(@CurrentUser UserPrincipal userPrincipal, @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/me/avatar")
                .toUriString();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        user.setImageUrl(fileName);
        userRepository.save(user);

        JSONObject obj = new JSONObject();
        obj.put("fileName", fileName);
        obj.put("fileDownloadUri", fileDownloadUri);
        obj.put("type", file.getContentType());
        obj.put("size", file.getSize());

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/user/me/avatar",
            method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAvatar(@CurrentUser UserPrincipal userPrincipal, HttpServletRequest request) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (user.getImageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        Resource resource = fileStorageService.loadFileAsResource(user.getImageUrl());

        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {

        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "/user/devices",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> devices(@CurrentUser UserPrincipal userPrincipal, Pageable pageable) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        boolean isAdmin = false;

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                isAdmin = true;
            }
        }

        return new ResponseEntity<>(isAdmin
                ? trustedDeviceRepository.findAllDevicesByCompanyId(user.getCompany().getId(), pageable)
                : trustedDeviceRepository.findAllDevicesByCompanyIdForUser(user.getCompany().getId(), pageable), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/users",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> users(Pageable pageable) {
        return new ResponseEntity<>(userRepository.findAll(pageable), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/users/{userId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> get(@PathVariable Long userId) {

        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/users/roles/set",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> add(@RequestBody UserRoleForm userRoleForm) {

        User user = userRepository.findById(userRoleForm.getUserId()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        List<Role> roles = new ArrayList<>();

        for (Long id : userRoleForm.getRoleIds()) {
            Role role = roleRepository.findById(id).orElseThrow(()
                    -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            messageSource.getMessage("http.status.code.404",
                                    null, LocaleContextHolder.getLocale())
                    )
            );

            roles.add(role);

        }

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/roles",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> roles() {
        return new ResponseEntity<>(roleRepository.findAll(), HttpStatus.OK);
    }

}
