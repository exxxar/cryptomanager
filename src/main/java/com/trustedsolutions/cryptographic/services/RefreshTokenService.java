package com.trustedsolutions.cryptographic.services;

import com.trustedsolutions.cryptographic.exception.TokenRefreshException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustedsolutions.cryptographic.model.RefreshToken;
import com.trustedsolutions.cryptographic.repository.RefreshTokenRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import java.util.List;

@Service
public class RefreshTokenService {

    @Value("${app.auth.tokenExpirationMsec}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
      public List<RefreshToken> findByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId);
    }
    

    public RefreshToken createRefreshTokenByEmail(String email) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findByEmail(email).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken createRefreshToken(Long userId) {
        //this.deleteByUserId(userId);//может быть лишнее todo: remove if not work
        
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
