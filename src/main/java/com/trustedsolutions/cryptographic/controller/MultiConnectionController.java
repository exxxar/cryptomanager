/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.controller;

import com.core.cryptolib.CryptoLoggerService;
import com.core.cryptolib.EncryptService;
import com.core.cryptolib.enums.ObjectType;
import com.core.cryptolib.forms.MultiConnectionForm;
import com.core.cryptolib.forms.TrustedDeviceForm;
import com.trustedsolutions.cryptographic.model.MultiConnection;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.repository.MultiConnectionRepository;
import com.trustedsolutions.cryptographic.repository.TrustedDeviceRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.HistoryOperationService;
import com.trustedsolutions.cryptographic.services.SettingsService;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author SAMS
 */
@RestController
public class MultiConnectionController {

    @Autowired
    MultiConnectionRepository mcRepository;

    @Autowired
    TrustedDeviceRepository tdRepository;

    @Autowired
    MessageSource messageSource;

    CryptoLoggerService logger;

    @Autowired
    HistoryOperationService historyOperationService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initialize() {
        this.logger = new CryptoLoggerService(
                "UUKK",
                "MultiConnectionController",
                Logger.getLogger(MultiConnectionController.class)
        );
        this.logger.setDebugMode(true);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/multi_connections",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> connections(Pageable pageable) {
        return new ResponseEntity<>(mcRepository.findAll(pageable), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/multi_connections/{mcId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> get(@PathVariable Long mcId) {

        MultiConnection mc = mcRepository.findById(mcId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                messageSource.getMessage("http.status.code.404",
                        null, LocaleContextHolder.getLocale())
        ));

        return new ResponseEntity<>(mc, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/multi_connections/remove/{mcId:[0-9]{1,100}}",
            method = RequestMethod.DELETE,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> remove(@CurrentUser UserPrincipal userPrincipal, @PathVariable Long mcId) {

        MultiConnection mc = mcRepository.findMultiConnectionById(mcId);

        if (mc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        mcRepository.delete(mc);

        return new ResponseEntity<>(mc, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/multi_connections/add",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> add(@CurrentUser UserPrincipal userPrincipal, @RequestBody @Valid MultiConnectionForm multiConnection, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("http.status.code.400",
                            null, LocaleContextHolder.getLocale())
            );
        }

        System.out.println(multiConnection);
        MultiConnection mc = new MultiConnection();
        mc.setTrustedDeviceMultiplyActualKey(multiConnection.getTrustedDeviceMultiplyActualKey());
        mc.setTrustedDeviceMultiplyOldKey(multiConnection.getTrustedDeviceMultiplyOldKey());

        if (multiConnection.getTrustedDeviceSenderId() != null) {
            TrustedDevice tdSender = tdRepository.findTrustedDeviceById(multiConnection.getTrustedDeviceSenderId());
            mc.setTdSender(tdSender);
        }

        if (multiConnection.getTrustedDeviceRecipientId() != null) {
            TrustedDevice tdRecipient = tdRepository.findTrustedDeviceById(multiConnection.getTrustedDeviceRecipientId());
            mc.setTdRecipient(tdRecipient);
        }

        System.out.println(mc);

        if (mc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        mc = mcRepository.save(mc);

        return new ResponseEntity<>(mc, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/multi_connections/update",
            method = RequestMethod.PUT,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> update(@CurrentUser UserPrincipal userPrincipal, @RequestBody @Valid MultiConnectionForm multiConnection, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("http.status.code.400",
                            null, LocaleContextHolder.getLocale())
            );
        }

        MultiConnection mc = mcRepository.findMultiConnectionById(multiConnection.getId());

        if (mc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        mc.setTrustedDeviceMultiplyActualKey(multiConnection.getTrustedDeviceMultiplyActualKey());
        mc.setTrustedDeviceMultiplyOldKey(multiConnection.getTrustedDeviceMultiplyOldKey());

        if (multiConnection.getTrustedDeviceSenderId() != null) {
            TrustedDevice tdSender = tdRepository.findTrustedDeviceById(multiConnection.getTrustedDeviceSenderId());
            mc.setTdSender(tdSender);
        }
        else
              mc.setTdSender(null);

        if (multiConnection.getTrustedDeviceRecipientId() != null) {
            TrustedDevice tdRecipient = tdRepository.findTrustedDeviceById(multiConnection.getTrustedDeviceRecipientId());
            mc.setTdRecipient(tdRecipient);
        }
        else
             mc.setTdRecipient(null);
            

        mc = mcRepository.save(mc);

        return new ResponseEntity<>(mc, HttpStatus.OK);
    }

}
