/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.listeners;

import com.core.cryptolib.enums.ObjectType;
import com.trustedsolutions.cryptographic.model.User;
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
public class UserListener {

    protected JSONObject tmp;

    @Autowired
    HistoryOperationService historyOperationService;

    public void setHistoryOperationService(HistoryOperationService historyOperationService) {
        this.historyOperationService = historyOperationService;
    }

    @PrePersist
    public void prePersist(User target) {
        this.tmp = target.toJSON();
    }

    @PostPersist
    public void postPersist(User target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After USER add", target.getId(), tmp, target.toJSON(), ObjectType.USER);

    }

    @Transactional
    @PreUpdate
    public void preUpdate(User target) {
        this.tmp = target.toJSON();
    }

    @Transactional
    @PostUpdate
    public void postUpdate(User target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After USER update", target.getId(),tmp, target.toJSON(), ObjectType.USER);

    }

    @PreRemove
    public void preDelete(User target) {
        this.tmp = target.toJSON();
    }

    @PostRemove
    public void postDelete(User target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After USER delete", target.getId(), tmp, target.toJSON(), ObjectType.USER);
    }
}
