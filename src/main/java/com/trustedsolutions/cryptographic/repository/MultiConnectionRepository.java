package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.MultiConnection;
import com.trustedsolutions.cryptographic.model.TrustedDevice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface MultiConnectionRepository extends PagingAndSortingRepository<MultiConnection, Long> {

    MultiConnection findMultiConnectionById(Long id);
//
//    MultiConnection findMultiConnectionByTrustedDeviceRecipientId(String trustedDeviceRecipientId);
//
//    MultiConnection findMultiConnectionByTrustedDeviceSenderId(String trustedDeviceSenderId);

    @Override
    Page<MultiConnection> findAll(Pageable p);

}
