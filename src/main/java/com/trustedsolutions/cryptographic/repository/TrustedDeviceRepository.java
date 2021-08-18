package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.TrustedDevice;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface TrustedDeviceRepository extends PagingAndSortingRepository<TrustedDevice, Long> {

    TrustedDevice findTrustedDeviceById(Long id);

    TrustedDevice findTrustedDeviceByDevicePublicId(String devicePublicId);

    TrustedDevice findTrustedDeviceByDevicePrivateId(String devicePrivateId);

    boolean existsTrustedDeviceByDevicePublicId(String devicePublicId);

    @Query(value = "SELECT * FROM trusted_device as table1 WHERE NOT EXISTS (SELECT * FROM company_trusted_devices as table2 WHERE table1.id = table2.trusted_devices_id)" , nativeQuery = true)
    Page<TrustedDevice> allFreeDevices(Pageable pageable);

    @Override
    Page<TrustedDevice> findAll(Pageable p);

}
