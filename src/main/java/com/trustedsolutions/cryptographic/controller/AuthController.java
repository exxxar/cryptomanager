package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.BadRequestException;
import com.trustedsolutions.cryptographic.model.AuthProvider;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.payload.ApiResponse;
import com.trustedsolutions.cryptographic.payload.AuthResponse;
import com.trustedsolutions.cryptographic.payload.LoginRequest;
import com.trustedsolutions.cryptographic.payload.SignUpRequest;
import com.trustedsolutions.cryptographic.repository.CompanyRepository;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenProvider tokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);
        
        List<Role> roles = new ArrayList<>();
        
        roles.add(roleRepository.findByName("ROLE_USER"));
        user.setRoles(roles);
        Company company = new Company();
        company.setActive(true);
        company.setCompanyName(signUpRequest.getName());
        company.setDescription("");
        company.setUserCheckUrl("");
        company = companyRepository.save(company);
        
        user.setCompany(company);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User result = userRepository.save(user);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();
        
        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully@"));
    }
    
}
