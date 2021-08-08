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

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        Role r1 = new Role("ROLE_ADMIN");
        Role r2 = new Role("ROLE_USER");
        Role r3 = new Role("ROLE_SECURE");

        if (roleRepository.count() == 0) {
            r1 = roleRepository.save(r1);
            r2 = roleRepository.save(r2);
            r3 = roleRepository.save(r3);
        }
        else
        {
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

//        user = new User();
//        user.setName("Test2");
//        user.setPassword(passwordEncoder.encode("test"));
//        user.setEmail("test2@test.com");
//       // user.setRole(2);
//        //user.setRoles(Arrays.asList(adminRole));
//        user.setEnabled(true);
//        userRepository.save(user);
//
        alreadySetup = true;
    }

}
