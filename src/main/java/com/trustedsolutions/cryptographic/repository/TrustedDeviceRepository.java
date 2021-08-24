package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import java.util.List;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface TrustedDeviceRepository extends PagingAndSortingRepository<TrustedDevice, Long> {

    TrustedDevice findTrustedDeviceById(Long id);

    TrustedDevice findTrustedDeviceByDevicePublicId(String devicePublicId);

    TrustedDevice findTrustedDeviceByDevicePrivateId(String devicePrivateId);

    boolean existsTrustedDeviceByDevicePublicId(String devicePublicId);

    //
    //@Query(value = "(SELECT * FROM trusted_device LEFT INNER JOIN company_trusted_devices ON (company_trusted_devices.`trusted_devices_id` = trusted_device.`id`) WHERE company_trusted_devices.`trusted_devices_id` IS NULL)" , nativeQuery = true)
   // @Query(value = "SELECT td FROM TrustedDevice td WHERE td.companies IS NULL")
    
    @Query(value = "SELECT * FROM trusted_device as table1 WHERE NOT EXISTS (SELECT * FROM company_trusted_devices as table2 WHERE table1.id = table2.trusted_devices_id)" , nativeQuery = true)
    //@Query(value = "(SELECT * FROM trusted_device INNER JOIN company_trusted_devices ON (company_trusted_devices.`trusted_devices_id` = trusted_device.`id`) WHERE company_trusted_devices.`trusted_devices_id` IS NULL)" , nativeQuery = true)
    Page<TrustedDevice> findAllFreeDevices(Pageable pageable);


    // 
    @Override
    Page<TrustedDevice> findAll(Pageable p);

}
