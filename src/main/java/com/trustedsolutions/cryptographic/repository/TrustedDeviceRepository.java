package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.TrustedDevice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface TrustedDeviceRepository extends PagingAndSortingRepository<TrustedDevice, Long> {

    TrustedDevice findTrustedDeviceById(Long id);

    TrustedDevice findTrustedDeviceByDevicePublicId(String devicePublicId);

    TrustedDevice findTrustedDeviceByDevicePrivateId(String devicePrivateId);

    boolean existsTrustedDeviceByDevicePublicId(String devicePublicId);

    @Override
    Page<TrustedDevice> findAll(Pageable p);

}
