package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    Optional<User> findByCompanyId(Long cpmpanyId);

    @Override
    Page<User> findAll(Pageable p);

    Boolean existsByEmail(String email);

}
