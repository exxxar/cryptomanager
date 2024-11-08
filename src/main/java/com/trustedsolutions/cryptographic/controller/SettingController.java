package com.trustedsolutions.cryptographic.controller;

import com.core.cryptolib.CryptoLoggerService;
import com.core.cryptolib.components.SettingObject;
import com.core.cryptolib.components.Settings;
import com.core.cryptolib.forms.SendMailForm;
import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.EmailService;

import com.trustedsolutions.cryptographic.services.HistoryOperationService;

import com.trustedsolutions.cryptographic.services.SettingsService;
import com.trustedsolutions.cryptographic.services.storage.FileSystemStorageService;
import com.trustedsolutions.cryptographic.util.PdfGenaratorUtil;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.MessageSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;

@RestController
public class SettingController {

    @Autowired
    MessageSource messageSource;

    @Value("${app.title}")
    private String appTitle;

    CryptoLoggerService logger;

    @Autowired
    SettingsService settingsService;

    @Autowired
    HistoryOperationService historyOperationService;

    @Autowired
    FileSystemStorageService fileStorageService;

    @Autowired
    PdfGenaratorUtil pdfGenaratorUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public EmailService emailService;

    /*
     GET /settings/firmware/download/{fileName:.+} - скачивание файла прошивки
     POST /settings/firmware/upload - загрузка прошивки на сервер
     DELETE /settings/firmware/remove - удаление файла прошивки с сервера (без отката к придущей)
     GET /settings/firmware/rollback - откат к преидущей прошивке
    
     GET /settings - список всех настроек
     POST /settings/update - обновление блока настроек
    
     GET /histories - списко всех операций по страницам
     */
    @PostConstruct
    public void initialize() {

        try {
            this.logger = new CryptoLoggerService(
                    appTitle,
                    "SettingsController",
                    Logger.getLogger(SettingController.class)
            );

        } catch (Exception e) {

        }

    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/settings",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> getAllSettings() throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {

        JSONArray array = new JSONArray();

        settingsService.getAllSettings().getSettings().stream().map(object -> {
            JSONObject tmp = new JSONObject();
            tmp.put(object.getKey(), object.getValue());
            tmp.put("type", object.getType());
            return tmp;
        }).forEachOrdered(tmp -> {
            array.add(tmp);
        });

        return new ResponseEntity<>(array, HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER","ROLE_SECURE"})
    @RequestMapping(value = "/settings/firmware",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> getActualFirmware() throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {

        JSONObject obj = new JSONObject();
        obj.put("firmware", settingsService.get("actualFirmware").getValue());

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/settings/update",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> settgins(@RequestBody List<SettingObject> settings) throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {

        Settings tmpSettings = new Settings();

        settings.forEach(obj -> {
           
            tmpSettings.put(obj);
        });

        settingsService.sync(tmpSettings);

        return new ResponseEntity<>(settingsService.getAllSettings(), HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN","ROLE_SECURE"})
    @RequestMapping(value = "/settings/firmware/upload",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3"})
    @ResponseBody
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("version") String version) {

        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/settings/firmware/download/")
                .path(fileName)
                .toUriString();

        if (!settingsService.get("actualFirmware").getValue().isBlank()
                && !settingsService.get("pathFirmware").getValue().isBlank()) {
            settingsService.put("previousFirmware", settingsService.get("actualFirmware").getValue());
            settingsService.put("pathPreviousFirmware", settingsService.get("pathFirmware").getValue());
        }

        settingsService.put("actualFirmware", version);
        settingsService.put("pathFirmware", settingsService.get("firmwareDirectory").getValue() + fileName);

        JSONObject obj = new JSONObject();
        obj.put("fileName", fileName);
        obj.put("fileDownloadUri", fileDownloadUri);
        obj.put("type", file.getContentType());
        obj.put("size", file.getSize());

        return new ResponseEntity<>(obj, HttpStatus.OK);

    }

//    @Secured("ROLE_ADMIN")
//    @RequestMapping(value = "/settings/firmware/uploadMultiple",
//            method = RequestMethod.POST,
//            headers = {"X-API-VERSION=0.0.3"})
//    @ResponseBody
//    public ResponseEntity<Object> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("version") String version) {
//
//        return new ResponseEntity<>(Arrays.asList(files)
//                .stream()
//                .map(file -> uploadFile(file))
//                .collect(Collectors.toList()), HttpStatus.OK);
//
//    }
    @Secured({"ROLE_ADMIN","ROLE_SECURE"})
    @RequestMapping(value = "/settings/firmware/download/{fileName:.+}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3"})
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
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

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/settings/firmware/remove",
            method = RequestMethod.DELETE,
            headers = {"X-API-VERSION=0.0.3"})
    @ResponseBody
    public ResponseEntity<Resource> remove(HttpServletRequest request) {

        settingsService.put("actualFirmware", "");
        settingsService.put("pathFirmware", "");

        String fileName = settingsService.get("pathFirmware").getValue();
        fileStorageService.deleteFile(fileName);

        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);

    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/settings/firmware/rollback",
            method = RequestMethod.DELETE,
            headers = {"X-API-VERSION=0.0.3"})
    @ResponseBody
    public ResponseEntity<Resource> roolback(HttpServletRequest request) {

        settingsService.put("actualFirmware", settingsService.get("previousFirmware").getValue());
        settingsService.put("pathFirmware", settingsService.get("pathPreviousFirmware").getValue());

        settingsService.put("previousFirmware", "");
        settingsService.put("pathPreviousFirmware", "");

        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);

    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/histories",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> histories(Pageable pageable) {
        return new ResponseEntity<>(historyOperationService.getAllHistoryOperations(pageable), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/histories/pdf",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    public ResponseEntity<Object> pdf() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", "James");
        Path path = pdfGenaratorUtil.createPdf("pdf", data);

        if (settingsService.isExist("adminEmail")) {
            emailService.sendMessageWithAttachment(
                    settingsService.get("adminEmail").getValue(),
                    messageSource.getMessage("mail.admin.pdf.title",
                            null,
                            LocaleContextHolder.getLocale()),
                    messageSource.getMessage("mail.admin.pdf.message",
                            null,
                            LocaleContextHolder.getLocale()),
                    path.toAbsolutePath().toString()
            );

            //Files.delete(path.toAbsolutePath());
        }

        JSONObject obj = new JSONObject();
        obj.put("path", path.toAbsolutePath().toString());
        obj.put("name", path.getFileName().toString());

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/histories/pdf",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    public ResponseEntity<Object> pdfEmail(@RequestBody SendMailForm emailForm) throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", "James");
        Path path = pdfGenaratorUtil.createPdf("pdf", data);

        emailService.sendMessageWithAttachment(
                emailForm.getEmail(),
                messageSource.getMessage("mail.admin.pdf.title",
                        null,
                        LocaleContextHolder.getLocale()),
                messageSource.getMessage("mail.admin.pdf.message",
                        null,
                        LocaleContextHolder.getLocale()),
                path.toAbsolutePath().toString()
        );

        //Files.delete(path.toAbsolutePath());
        JSONObject obj = new JSONObject();
        obj.put("path", path.toAbsolutePath().toString());
        obj.put("name", path.getFileName().toString());

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/histories/owner",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> historiesOwner(@CurrentUser UserPrincipal userPrincipal, Pageable pageable) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        boolean isAdmin = false;

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                isAdmin = true;
            }
        }

        return new ResponseEntity<>(isAdmin
                ? historyOperationService.getUserHistory(userPrincipal.getId(), pageable)
                : historyOperationService.getUserHistoryForUser(userPrincipal.getId(), pageable),
                HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/histories/user/{userId}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> historiesUser(@CurrentUser UserPrincipal userPrincipal, @PathVariable Long userId, Pageable pageable) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        boolean isAdmin = false;

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                isAdmin = true;
            }
        }

        return new ResponseEntity<>(isAdmin
                ? historyOperationService.getUserHistory(userId, pageable)
                : historyOperationService.getUserHistoryForUser(userId, pageable),
                HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/histories/object/{objectId}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> historiesObject(@PathVariable Long objectId, Pageable pageable) {
        return new ResponseEntity<>(historyOperationService.getObjectHistory(objectId, pageable), HttpStatus.OK);
    }
}
