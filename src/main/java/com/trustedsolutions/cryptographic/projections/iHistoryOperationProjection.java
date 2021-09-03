/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.projections;

import com.core.cryptolib.enums.ObjectType;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author SAMS
 */
public interface iHistoryOperationProjection {

    public Long getId();

    @Value("#{target.create_date_time}")
    public LocalDateTime getCreateDateTime();

    @Value("#{target.update_date_time}")
    public LocalDateTime getUpdateDateTime();

    public String getDescription();

    @Value("#{target.object_id}")
    public Long getObjectId();

    @Value("#{target.object_type}")
    public ObjectType getObjectType();

    @Value("#{target.user_id}")
    public Long getUserId();

}
