/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.forms.CompanyForm;
import com.trustedsolutions.cryptographic.forms.CompanyTrdustedDeviceAssignForm;
import com.trustedsolutions.cryptographic.forms.UserCheckForm;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.CompanyRepository;

import com.trustedsolutions.cryptographic.repository.TrustedDeviceRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.ParameterStringBuilder;
import org.springframework.data.domain.Pageable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CompaniesController {

    @Autowired
    MessageSource messageSource;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    TrustedDeviceRepository trustedDeviceRepository;

    @Autowired
    private UserRepository userRepository;


    /*
    
    GET /companies - список активных компаний
    POST /companies/add - добавление компании
    
    DELETE /companies/remove/{compay_id} - список активных компаний
    
    GET /companies/{compay_id} - получение информации о компании
    PUT /companies/update/{compay_id} - редактирование компании

    POST /companies/user_check - проверка принадлежности пользователя компании
    
    GET /companies/{company_id}/devices - список всех устройств компании
    GET /companies/{company_id}/devices/{device_id} - информация по выбранному доверенному устройству
    
    GET /companies/{company_id}/access - получение текущего состояния активности компании
    POST /companies/{company_id}/access - изменение состояния активности компании: активна\не активна, заблокирована \ не заблокирована (?)
    
    POST /companies/devices/attach - ассоциирование доверенных устройств с компанией
    POST /companies/devices/detach - убирает доверенное устройств из компанией
     */
    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/add",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> add(@Valid @RequestBody CompanyForm companyForm) {

        Company company = new Company(companyForm);
        company = (Company) companyRepository.save(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/update/{companyId:[0-9]{1,100}}",
            method = RequestMethod.PUT,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> update(@PathVariable Long companyId, @Valid @RequestBody CompanyForm companyForm) {

        System.out.println("ID=>" + companyId);
        System.out.println("CompanyForm=>" + companyForm.toString());
        Company company = (Company) companyRepository.findById(companyId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        company.setCompany(companyForm);

        companyRepository.save(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "/companies/update/self",
            method = RequestMethod.PUT,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> selfUpdate(@CurrentUser UserPrincipal userPrincipal, @Valid @RequestBody CompanyForm companyForm) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        Company company = (Company) companyRepository.findById(user.getCompany().getId()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        company.setCompany(companyForm);

        companyRepository.save(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @RequestMapping(value = "/companies/user_check",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> checkUser(@Valid @RequestBody UserCheckForm userCheckForm) throws MalformedURLException, ProtocolException, IOException, ParseException {

        Company company = companyRepository.findById(userCheckForm.getCompanyId()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                )
        );

        try {
            URL url = new URL(company.getUserCheckUrl());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("userId", userCheckForm.getUserId());

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();

            int status = con.getResponseCode();

            if (status != HttpStatus.OK.value()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        messageSource.getMessage("http.status.code.404",
                                null, LocaleContextHolder.getLocale())
                );
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            JSONParser jp = new JSONParser();
            JSONObject message = (JSONObject) jp.parse(content.toString());

            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> companies(Pageable pageable,
            @RequestParam(defaultValue = "-1") Long id,
            @RequestParam(defaultValue = "") String filter
    ) {

        Page<Company> page;
        if (id > 0 && !filter.isEmpty()) {
            page = companyRepository.findAll(pageable);
        } else {
            page = companyRepository.findAllByInputString(
                    id,
                    filter,
                    pageable);
        }

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/{companyId:[0-9]{1,100}}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> get(@PathVariable Long companyId) {

        System.out.println("ID=>" + companyId);

        Company company = companyRepository.findCompanyById(companyId);

        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/{companyId:[0-9]{1,100}}/owner",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> owner(@PathVariable Long companyId) {

        User user = userRepository.findByCompanyId(companyId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                messageSource.getMessage("http.status.code.404",
                        null, LocaleContextHolder.getLocale())
        ));

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/{companyId:[0-9]{1,100}}/devices",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> devices(@PathVariable Long companyId, Pageable pageable) {

        Page<TrustedDevice> td = trustedDeviceRepository.findAllDevicesByCompanyId(companyId, pageable);

        if (td == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        return new ResponseEntity<>(td, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/{companyId:[0-9]{1,100}}/devices/{deviceId}",
            method = RequestMethod.GET,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> device(@PathVariable Long companyId, @PathVariable Long deviceId) {

        Company company = companyRepository.findCompanyById(companyId);

        List<TrustedDevice> tdList = new LinkedList<>();

        company.getTrustedDevices().forEach(obj -> {
            if (Objects.equals(obj.getId(), deviceId)) {
                tdList.add(obj);
            }
        });

        if (tdList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        return new ResponseEntity<>(tdList.get(0), HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/remove/{companyId:[0-9]{1,100}}",
            method = RequestMethod.DELETE,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> remove(@PathVariable Long companyId) {

        Company company = companyRepository.findCompanyById(companyId);

        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        companyRepository.delete(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/devices/attach",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> attach(@Valid @RequestBody CompanyTrdustedDeviceAssignForm companyTrdustedDeviceAssignForm) {

        Company company = companyRepository.findCompanyById(companyTrdustedDeviceAssignForm.getCompanyId());

        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        TrustedDevice device = trustedDeviceRepository.findTrustedDeviceById(companyTrdustedDeviceAssignForm.getTrustedDeviceId());

        if (device == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        company.getTrustedDevices().add(device);

        companyRepository.save(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/companies/devices/detach",
            method = RequestMethod.POST,
            headers = {"X-API-VERSION=0.0.3", "content-type=application/json"})
    @ResponseBody
    public ResponseEntity<Object> detach(@Valid @RequestBody CompanyTrdustedDeviceAssignForm companyTrdustedDeviceAssignForm) {

        Company company = companyRepository.findCompanyById(companyTrdustedDeviceAssignForm.getCompanyId());

        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }

        TrustedDevice device = trustedDeviceRepository.findTrustedDeviceById(companyTrdustedDeviceAssignForm.getTrustedDeviceId());

        if (device == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("http.status.code.404",
                            null, LocaleContextHolder.getLocale())
            );
        }
        company.getTrustedDevices().remove(device);

        companyRepository.save(company);

        return new ResponseEntity<>(company, HttpStatus.OK);
    }

}
