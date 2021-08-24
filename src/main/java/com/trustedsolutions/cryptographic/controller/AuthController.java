package com.trustedsolutions.cryptographic.controller;

import com.trustedsolutions.cryptographic.exception.BadRequestException;
import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.exception.TokenRefreshException;
import com.trustedsolutions.cryptographic.forms.PasswordForm;
import com.trustedsolutions.cryptographic.forms.ResetPasswordForm;
import com.trustedsolutions.cryptographic.model.AuthProvider;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.PasswordResetToken;
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
import com.trustedsolutions.cryptographic.repository.PasswordResetTokenRepository;
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
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${app.client.url}")
    private String appClientUrl;

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

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    @PostMapping("/simple-reset")
    public ResponseEntity<Object> simpleReset(@Valid @RequestBody ResetPasswordForm resetPasswordForm) {

        User user = userRepository.findByEmail(resetPasswordForm.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Email not found!")
                );;

        String tmpPassword = UUID.randomUUID().toString();
        String password = passwordEncoder.encode(tmpPassword);

        user.setPassword(password);
        userRepository.save(user);

        emailService.sendSimpleMessage(resetPasswordForm.getEmail(), "reset password",
                "new password: " + tmpPassword);

        JSONObject obj = new JSONObject();
        obj.put("code", HttpStatus.OK);
        obj.put("detail", "Password reset success");
        obj.put("title", "Reset password");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Email not found!");
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
        company.setName(signUpRequest.getName());
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

        JSONObject obj = new JSONObject();
        obj.put("code", HttpServletResponse.SC_OK);
        obj.put("detail", "Logout success");
        obj.put("title", "Logout");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(final HttpServletRequest request, @Valid @RequestBody ResetPasswordForm resetPasswordForm) {
        User user = userRepository.findByEmail(resetPasswordForm.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Email not found!")
                );

        final String token = UUID.randomUUID().toString();
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);

        final String url = settingsService.get("appClientUrl", appClientUrl) + "/reset/change?token=" + token;

        emailService.sendSimpleMessage(user.getEmail(), "Reset password", url);

        JSONObject obj = new JSONObject();
        obj.put("code", HttpServletResponse.SC_OK);
        obj.put("detail", "Reset password send success");
        obj.put("title", "Reset password");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    // Save password
    @PostMapping("/reset/save")
    public ResponseEntity<?> savePassword(@RequestBody PasswordForm passwordDto) {

        PasswordResetToken passToken = passwordTokenRepository.findByToken(passwordDto.getToken());

        if (passToken == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Token not valid!");
        }
        User user = passToken.getUser();

        if (!isValidateResetToken(passToken.getToken())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Token not valid!");
        }

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User not found!");
        }

        String password = passwordEncoder.encode(passwordDto.getNewPassword());

        user.setPassword(password);
        userRepository.save(user);

        JSONObject obj = new JSONObject();
        obj.put("code", HttpServletResponse.SC_OK);
        obj.put("detail", "Change password success");
        obj.put("title", "Change password");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @PostMapping("/reset/update")
    public ResponseEntity<?> changeUserPassword(@CurrentUser UserPrincipal userPrincipal, @RequestBody PasswordForm passwordDto) {
        User user = userRepository.findByEmail(userPrincipal.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Email not found!")
        );

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Bad old password!");
        }

        user.setPassword(passwordDto.getNewPassword());
        userRepository.save(user);

        JSONObject obj = new JSONObject();
        obj.put("code", HttpServletResponse.SC_OK);
        obj.put("detail", "Change password success");
        obj.put("title", "Change password");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    private boolean isValidateResetToken(String token) {
        final PasswordResetToken verificationToken = passwordTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return false;
        }

        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime()
                        .getTime()) <= 0) {
            passwordTokenRepository.delete(verificationToken);
            return false;
        }

        passwordTokenRepository.delete(verificationToken);

        return true;
    }
}
