/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.config;

import com.trustedsolutions.cryptographic.model.AuthProvider;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.Privilege;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.CompanyRepository;
import com.trustedsolutions.cryptographic.repository.PrivilegeRepository;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.services.SettingsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author SAMS
 */
@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    SettingsService settingsService;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        if (settingsService.getSettingsCount() == 0) {
            settingsService.put("actualFirmware", "");
            settingsService.put("previousFirmware", "");
            settingsService.put("pathFirmware", "");
            settingsService.put("pathPreviousFirmware", "");
            settingsService.put("adminEmail", "exxxar@gmail.com");
            settingsService.put("maxDeviceOldKeyLifetime", "864000000");
        }

        Role r1 = new Role("ROLE_ADMIN");
        Role r2 = new Role("ROLE_USER");
        Role r3 = new Role("ROLE_SECURE");

        if (roleRepository.count() == 0) {
            r1 = roleRepository.save(r1);
            r2 = roleRepository.save(r2);
            r3 = roleRepository.save(r3);
        } else {
            r1 = roleRepository.findByName("ROLE_ADMIN");
            r2 = roleRepository.findByName("ROLE_USER");
            r3 = roleRepository.findByName("ROLE_SECURE");
        }

        List<Role> roles = new ArrayList<>();

        if (!userRepository.existsByEmail("admin@admin.com")) {
            roles.add(r1);
            roles.add(r2);
            roles.add(r3);

            User user = new User();
            user.setName("admin");
            user.setEmail("admin@admin.com");
            user.setPassword("password");
            user.setProvider(AuthProvider.local);

            roles.add(roleRepository.findByName("ROLE_USER"));
            user.setRoles(roles);
            Company company = new Company();
            company.setActive(true);
            company.setCompanyName("MACSOFT");
            company.setDescription("");
            company.setUserCheckUrl("");
            company = companyRepository.save(company);

            user.setCompany(company);
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User result = userRepository.save(user);
        }

        alreadySetup = true;
    }

}
