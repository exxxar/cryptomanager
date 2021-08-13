package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.RefreshToken;
import com.trustedsolutions.cryptographic.model.User;
import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends PagingAndSortingRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUserId(Long userId);

    @Modifying
    int deleteByUser(User user);
}
