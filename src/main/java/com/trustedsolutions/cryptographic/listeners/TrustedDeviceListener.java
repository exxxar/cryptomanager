/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.listeners;

import com.core.cryptolib.enums.ObjectType;
import com.trustedsolutions.cryptographic.model.TrustedDevice;
import com.trustedsolutions.cryptographic.services.AutowireHelper;
import com.trustedsolutions.cryptographic.services.HistoryOperationService;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author SAMS
 */
@Component
public class TrustedDeviceListener {

    protected JSONObject tmp;

    @Autowired
    HistoryOperationService historyOperationService;

    public void setHistoryOperationService(HistoryOperationService historyOperationService) {
        this.historyOperationService = historyOperationService;
    }

    @PrePersist
    public void prePersist(TrustedDevice target) {
        this.tmp = target.toJSON();
    }

    @PostPersist
    public void postPersist(TrustedDevice target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After TRUSTED_DEVICE add", target.getId(), tmp, target.toJSON(), ObjectType.TRUSTED_DEVICE);

    }

    @Transactional
    @PreUpdate
    public void preUpdate(TrustedDevice target) {
        this.tmp = target.toJSON();
    }

    @Transactional
    @PostUpdate
    public void postUpdate(TrustedDevice target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After TRUSTED_DEVICE update", target.getId(),tmp, target.toJSON(), ObjectType.TRUSTED_DEVICE);

    }

    @PreRemove
    public void preDelete(TrustedDevice target) {
        this.tmp = target.toJSON();
    }

    @PostRemove
    public void postDelete(TrustedDevice target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After TRUSTED_DEVICE delete", target.getId(), tmp, target.toJSON(), ObjectType.TRUSTED_DEVICE);
    }
}
