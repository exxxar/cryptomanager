/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.controller;

import com.core.cryptolib.EncryptService;
import com.core.cryptolib.CryptoLoggerService;
import com.core.cryptolib.UserPayloadServiceForCrypt;
import com.core.cryptolib.enums.InfoRequestType;
import com.core.cryptolib.enums.ObjectType;
import com.core.cryptolib.forms.InfoRequestForm;
import com.core.cryptolib.forms.ResponseTDPublicIdForm;
import com.core.cryptolib.forms.TransferDataForm;
import com.core.cryptolib.forms.TransferDataFormWithDevicesInfo;
import com.core.cryptolib.forms.TrustedDeviceForm;
import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.model.Role;

import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.TrustedDeviceRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.HistoryOperationService;
import com.trustedsolutions.cryptographic.services.SettingsService;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

@RestController
public class TrustedDeviceController {

    /*
    
    GET     /trusted_devices - список доверенных устройств с пагинацией
    
    POST    /trusted_devices/add - добавление нового устройства
    POST    /trusted_devices/reencrypt - перешифрование данных
    PUT     /trusted_devices/update - обновление параметров устройства
    GET     /trusted_devices/{trusted_device_id} - получение получение информации о доверенном устройстве по его идентификатору
    DELETE  /trusted_devices/remove/{trusted_device_id} - удаление доверенного устройства по идентификатору
    
     */
    @Autowired
    TrustedDeviceRepository tdRepository;

    @Autowired
    MessageSource messageSource;

    EncryptService desApp;

    @Value("${cryptographic.attempts}")
    private int maxAttempts;

    CryptoLoggerService logger;

   

    @Autowired
    SettingsService settingsService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initialize() {
        this.logger = new CryptoLoggerService(
                "UUKK",
                "TrustedDeviceController",
                Logger.getLogger(TrustedDeviceController.class)
        );
        this.logger.setDebugMode(true);
        this.desApp = new EncryptService();
    }

//    @Secured({"ROLE_USER", "ROLE_ADMIN"})
//    @RequestMapping(value = "/trusted_devices/check/{trudtedDevicePublicId:[0-9]{1,100}}",
//            method = RequestMethod.GET,
//            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
//    @ResponseBody
//    public ResponseEntity<Object> check(@PathVariable("trudtedDevicePublicId") String trustedDevicePublicId) throws UnsupportedEncodingException {
//
//        boolean isExist = tdRepository.existsTrustedDeviceByDevicePublicId(trustedDevicePublicId);
//
//        JSONObject message = new JSONObject();
//        message.put("is_exist", isExist);
//
//        UserPayloadServiceForCrypt ups = new UserPayloadServiceForCrypt();
//
//        if (!isExist) {
//
//            InfoRequestForm infoRequestForm = new InfoRequestForm();
//
//            infoRequestForm.setId(1l);
//            infoRequestForm.setRecipientUserId("testid");
//            infoRequestForm.setSenderUserId("testid");
//
//            return new ResponseEntity<>(ups.denailRequest().toBase64SimpleJSON(),
//                    HttpStatus.OK);
//        }
//
//        return new ResponseEntity<>(message, HttpStatus.OK);
//    }
    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/trusted_devices/activate/{deviceId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> activate(@CurrentUser UserPrincipal userPrincipal, @PathVariable("deviceId") Long deviceId) throws UnsupportedEncodingException {

        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Email not found!")
                );

        if (!tdRepository.userHasDeviceByCompanyAndDeviceIds(deviceId, user.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Email not found!");
        }

        TrustedDevice td = tdRepository.findTrustedDeviceById(deviceId);
        td.setActive(!td.isActive());

        tdRepository.save(td);

        JSONObject message = new JSONObject();
        message.put("is_active", td.isActive());

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

     @Secured({"ROLE_SECURE"})
    @RequestMapping(value = "/trusted_devices/enable/{deviceId:[a-zA-Z0-9-_]{2,512}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> enableDevice(@CurrentUser UserPrincipal userPrincipal, @PathVariable("deviceId") String devicePublicId) throws UnsupportedEncodingException {

        TrustedDevice td =tdRepository.findTrustedDeviceByDevicePublicId(devicePublicId);
        td.setActive(!td.isActive());

        tdRepository.save(td);

        JSONObject message = new JSONObject();
        message.put("is_active", td.isActive());

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

      @Secured({"ROLE_ADMIN","ROLE_SECURE"})
    @RequestMapping(value = "/trusted_devices/all-public-ids",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> allDevices() {

        JSONArray list = new JSONArray();
        
        List<TrustedDevice> tdList = (List<TrustedDevice>)tdRepository.findAll();
        
        for(TrustedDevice td:tdList)
            list.add(td.getDevicePublicId());

        return new ResponseEntity<>(list, HttpStatus.OK);

    }
    
    @Secured({"ROLE_ADMIN","ROLE_SECURE"})
    @RequestMapping(value = "/trusted_devices/free",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> freeDevices(@CurrentUser UserPrincipal userPrincipal, Pageable pageable) {

      
        return new ResponseEntity<>(tdRepository.findAllFreeDevices(pageable), HttpStatus.OK);

    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> devices(Pageable pageable) {
        return new ResponseEntity<>(tdRepository.findAll(pageable), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices/{tdId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> get(@PathVariable Long tdId) {

        TrustedDevice td = tdRepository.findById(tdId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                messageSource.getMessage("http.status.code.404",
                        null, LocaleContextHolder.getLocale())
        ));

        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices/remove/{tdId:[0-9]{1,100}}",
            method = RequestMethod.DELETE,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> remove(@CurrentUser UserPrincipal userPrincipal, @PathVariable Long tdId) {

        TrustedDevice td = tdRepository.findTrustedDeviceById(tdId);

       

        if (td == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        tdRepository.delete(td);

       

        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices/add",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> add(@CurrentUser UserPrincipal userPrincipal, @RequestBody @Valid TrustedDeviceForm trustedDevice, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("http.status.code.400",
                            null, LocaleContextHolder.getLocale())
            );
        }

        if (tdRepository.findTrustedDeviceByDevicePublicId(trustedDevice.getDevicePublicId()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("http.status.code.400",
                            null, LocaleContextHolder.getLocale())
            );
        }

        TrustedDevice td = new TrustedDevice(trustedDevice);

        if (td == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        td = tdRepository.save(td);

        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices/update",
            method = RequestMethod.PUT,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> update(@CurrentUser UserPrincipal userPrincipal, @RequestBody @Valid TrustedDeviceForm trustedDevice, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("http.status.code.400",
                            null, LocaleContextHolder.getLocale())
            );
        }

        TrustedDevice td = tdRepository.findTrustedDeviceById(trustedDevice.getId());

        if (td == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        td.setTrustedDevice(trustedDevice);

        td = tdRepository.save(td);

        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    @Secured({"ROLE_ADMIN"})
    @RequestMapping(value = "/trusted_devices/reset/{tdId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> reset(@PathVariable("tdId") Long tdId) throws NoSuchAlgorithmException {
        TrustedDevice td = tdRepository.findById(tdId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        // td.setDeviceActualKeyEncode(td.getDeviceResetKey());
        //td.setDeviceOldKeyEncode(td.getDeviceResetKey());
        td.setAcceptAutoReset(!td.isAcceptAutoReset());
        td.setResetTry(false);
        tdRepository.save(td);

        return new ResponseEntity<>(td.toJSON(), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/trusted_devices/get/{deviceId:[a-zA-Z0-9-_]{2,512}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> get(@PathVariable("deviceId") String trustedDevicePublicId) {
        TrustedDevice td = tdRepository.findTrustedDeviceByDevicePublicId(trustedDevicePublicId);

        if (td == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }
        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    // @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/trusted_devices/reencrypt",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> reencrypt(@RequestBody InfoRequestForm infoRequestForm) throws ParseException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException {

        this.logger.info("start reencrypt!");

        byte[] decrypted_data = Base64.getDecoder().decode(infoRequestForm.getData().getBytes());

        UserPayloadServiceForCrypt ups = new UserPayloadServiceForCrypt(
                settingsService.getAllSettings(),
                logger);

        JSONObject tmp = null;
        try {
            JSONParser parser = new JSONParser();
            tmp = (JSONObject) parser.parse(new String(decrypted_data));

        } catch (ParseException ex) {

            logger.info(String.format("Message:%s Data:%s",
                    ex.getMessage(),
                    infoRequestForm.getData()
            ));

            infoRequestForm.setData(ups.denailRequest().toBase64SimpleJSON());

            return new ResponseEntity<>(infoRequestForm.toJSON(),
                    HttpStatus.OK);
        }

        TransferDataForm tdf = new TransferDataForm();
        TransferDataFormWithDevicesInfo tdfWdi = null;

        tdf.setDataBase64((String) tmp.get("data"));
        tdf.setType(Integer.parseInt(tmp.get("type").toString()));

        TrustedDevice tdRecipient = null;

        TrustedDevice tdSender = null;

        try {

            ResponseTDPublicIdForm resp = ups.twiceEncryptedPermissionBegin(tdf);
            this.logger.info("st 1");
            if (resp == null) {
                tdf = ups.denailRequest();

            } else {

                tdRecipient = tdRepository.findTrustedDeviceByDevicePublicId(
                        resp.getTdRecipientTrustedDevicePublicId());
                this.logger.info("st 2");
                tdSender = tdRepository.findTrustedDeviceByDevicePublicId(
                        resp.getTdSenderTrustedDevicePublicId());
                this.logger.info("st 3");
                if (tdSender == null || tdRecipient == null) {

                    this.logger.info("st 4");
                    infoRequestForm.setData(ups.denailRequest().toBase64SimpleJSON());
                    return new ResponseEntity<>(infoRequestForm.toJSON(),
                            HttpStatus.OK);
                }

                byte[] senderDeviceNewKey = EncryptService.getSecureRandom(8);

                byte[] recipientDeviceNewKey = EncryptService.getSecureRandom(8);

                byte[] recipientDeviceResetKey = EncryptService.getSecureRandom(8);

                byte[] senderDeviceResetKey = EncryptService.getSecureRandom(8);
                this.logger.info("st 5");
                tdfWdi = ups.twiceEncryptedPermissionEnd(
                        tdRecipient.toTrustedDeviceForm(),
                        tdSender.toTrustedDeviceForm(),
                        senderDeviceNewKey,
                        recipientDeviceNewKey,
                        recipientDeviceResetKey,
                        senderDeviceResetKey,
                        maxAttempts
                );
                this.logger.info("st 6");
                tdf = tdfWdi.getTdf();

                tdRecipient.setAttempts(tdfWdi.getRecipient().getAttempts());
                tdRecipient.setCurrentFirmware(tdfWdi.getRecipient().getCurrentFirmware());
                tdRecipient.setResetTry(tdfWdi.getRecipient().isResetTry());
                tdRecipient.setDeviceResetKey(tdfWdi.getRecipient().getDeviceResetKey());
                tdRepository.save(tdRecipient);
                this.logger.info("st 7");
                tdSender.setAttempts(tdfWdi.getSender().getAttempts());
                tdSender.setCurrentFirmware(tdfWdi.getSender().getCurrentFirmware());
                tdSender.setResetTry(tdfWdi.getSender().isResetTry());
                tdSender.setDeviceResetKey(tdfWdi.getSender().getDeviceResetKey());
                tdRepository.save(tdSender);
                this.logger.info("st 8");
                if (tdf.getType() == InfoRequestType.denial.getValue()) {

                    logger.info(String.format("*Current type*=%d *needed type*=%d\n*Data*:\n%s",
                            tdf.getType(),
                            InfoRequestType.twiceEncryptedRequest.getValue(),
                            infoRequestForm.getData()
                    ));
                    infoRequestForm.setData(ups.denailRequest().toBase64SimpleJSON());

                    return new ResponseEntity<>(infoRequestForm.toJSON(),
                            HttpStatus.OK);
                }

                logger.info("reencrypt step6");

                byte[] tmp_recipient_actual_key = tdRecipient.getDeviceActualKey();
                byte[] tmp_recipient_reset_key = tdRecipient.getDeviceResetKey();
                byte[] tmp_sender_actual_key = tdSender.getDeviceActualKey();
                byte[] tmp_sender_reset_key = tdSender.getDeviceResetKey();

                tdRecipient.setDeviceActualKeyEncode(recipientDeviceNewKey);
                tdRecipient.setDeviceOldKeyEncode(tmp_recipient_actual_key);
                tdRecipient.setDeviceResetKeyEncode(tmp_recipient_reset_key);
                tdRecipient.setLastUpdateActualKeyDateTime(LocalDateTime.now());
                tdRecipient.setAttempts(0);
                tdRepository.save(tdRecipient);
                this.logger.info("st 9");
                tdSender.setDeviceActualKeyEncode(senderDeviceNewKey);
                tdSender.setDeviceOldKeyEncode(tmp_sender_actual_key);
                tdSender.setDeviceResetKeyEncode(tmp_sender_reset_key);
                tdSender.setLastUpdateActualKeyDateTime(LocalDateTime.now());
                tdSender.setAttempts(0);
                tdRepository.save(tdSender);

                logger.info("reencrypt step7");

            }

        } catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | ParseException ex) {
            logger.info(String.format("%s\n*Data*:\n%s",
                    ex.getMessage(),
                    infoRequestForm.getData()
            ));

            infoRequestForm.setData(ups.denailRequest().toBase64SimpleJSON());
            logger.info("reencrypt step8");
            return new ResponseEntity<>(infoRequestForm.toJSON(),
                    HttpStatus.OK);
        }

        logger.info("reencrypt step11");

        settingsService.sync(ups.getSettings());

        infoRequestForm.setData(Base64
                .getEncoder()
                .encodeToString(tdf.toJSON()
                        .toJSONString().getBytes()));

        return new ResponseEntity<>(infoRequestForm.toJSON(), HttpStatus.OK);
    }
}
