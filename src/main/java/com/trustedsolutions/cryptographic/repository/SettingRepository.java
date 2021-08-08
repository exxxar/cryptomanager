package com.trustedsolutions.cryptographic.repository;


import com.trustedsolutions.cryptographic.model.Setting;
import org.springframework.data.repository.CrudRepository;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface SettingRepository extends CrudRepository<Setting, Long> {

    Setting findSettingById(Long id);

    Setting findBySettingKey(String settingKey);

}
