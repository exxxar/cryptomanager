/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.forms;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author SAMS
 */
public interface iTrustedDeviceProjection {

    public Long getId();

    public void setId(Long id);

    public boolean isActive();

    public int getAttempts();
    
    public String getDescription();

    @Value("#{target.current_firmware}")
    public String getCurrentFirmware();

    @Value("#{target.device_public_id}")
    public String getDevicePublicId();

    @Value("#{target.last_update_actual_key_date_time}")
    public LocalDateTime getLastUpdateActualKeyDateTime();

    @Value("#{target.create_date_time}")
    public LocalDateTime getCreateDateTime();

}
