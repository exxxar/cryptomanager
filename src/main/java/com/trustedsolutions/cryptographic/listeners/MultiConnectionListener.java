/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.listeners;

import com.core.cryptolib.enums.ObjectType;
import com.trustedsolutions.cryptographic.model.Company;
import com.trustedsolutions.cryptographic.model.MultiConnection;
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
public class MultiConnectionListener {

    protected JSONObject tmp;

    @Autowired
    HistoryOperationService historyOperationService;

    public void setHistoryOperationService(HistoryOperationService historyOperationService) {
        this.historyOperationService = historyOperationService;
    }

    @PrePersist
    public void prePersist(Company target) {
        this.tmp = target.toJSON();
    }

    @PostPersist
    public void postPersist(MultiConnection target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After MULTI_CONNECTION add", target.getId(), tmp, target.toJSON(), ObjectType.MULTI_CONNECTION);

    }

    @Transactional
    @PreUpdate
    public void preUpdate(MultiConnection target) {
        this.tmp = target.toJSON();
    }

    @Transactional
    @PostUpdate
    public void postUpdate(MultiConnection target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After MULTI_CONNECTION update", target.getId(),tmp, target.toJSON(), ObjectType.MULTI_CONNECTION);

    }

    @PreRemove
    public void preDelete(Company target) {
        this.tmp = target.toJSON();
    }

    @PostRemove
    public void postDelete(MultiConnection target) {
        AutowireHelper.autowire(this);
        historyOperationService.store("After MULTI_CONNECTION delete", target.getId(), tmp, target.toJSON(), ObjectType.MULTI_CONNECTION);
    }
}
