package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.forms.CompanyForm;
import com.trustedsolutions.cryptographic.forms.UserRoleForm;
import com.trustedsolutions.cryptographic.forms.UserSelfUpdateForm;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.TrustedDeviceRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

        if (userSelfUpdateForm.getPassword() != null && userSelfUpdateForm.getConfirmPassword() != null) {

            user.setPassword(userSelfUpdateForm.getPassword());
        }

        if (userSelfUpdateForm.getName() != null) {
            user.setName(userSelfUpdateForm.getName());
        }

        user.setEnabled(userSelfUpdateForm.getEnabled());

        user = userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
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
