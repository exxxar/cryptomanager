package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.BadRequestException;
import com.trustedsolutions.cryptographic.exception.TokenRefreshException;
import com.trustedsolutions.cryptographic.model.AuthProvider;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.RefreshToken;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.payload.ApiResponse;
import com.trustedsolutions.cryptographic.payload.AuthResponse;
import com.trustedsolutions.cryptographic.payload.LogOutRequest;
import com.trustedsolutions.cryptographic.payload.LoginRequest;
import com.trustedsolutions.cryptographic.payload.SignUpRequest;
import com.trustedsolutions.cryptographic.payload.TokenRefreshRequest;
import com.trustedsolutions.cryptographic.payload.TokenRefreshResponse;
import com.trustedsolutions.cryptographic.repository.CompanyRepository;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.CurrentUser;
import com.trustedsolutions.cryptographic.security.TokenProvider;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import com.trustedsolutions.cryptographic.services.EmailService;
import com.trustedsolutions.cryptographic.services.RefreshTokenService;
import com.trustedsolutions.cryptographic.services.SettingsService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;

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

    @Autowired
    public EmailService emailService;

    @Autowired
    SettingsService settingsService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshTokenByEmail(loginRequest.getEmail());
        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token, refreshToken.getToken()));

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

        emailService.sendSimpleMessage(signUpRequest.getEmail(), "registration", "thank");
        if (settingsService.isExist("adminEmail")) {
            emailService.sendSimpleMessage(settingsService.get("adminEmail").getValue(), "new user", signUpRequest.getEmail());
        }

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully@"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenProvider.generateTokenFromUsername(user.getName());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<?> logoutUser(@CurrentUser UserPrincipal userPrincipal) {
        refreshTokenService.deleteByUserId(userPrincipal.getId());
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

}
