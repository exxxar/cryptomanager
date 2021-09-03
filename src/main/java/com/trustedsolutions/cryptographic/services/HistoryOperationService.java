/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.services;

import com.core.cryptolib.enums.ObjectType;
import com.trustedsolutions.cryptographic.projections.iHistoryOperationProjection;
import com.trustedsolutions.cryptographic.model.HistoryOperation;

import com.trustedsolutions.cryptographic.repository.HistoryOperationRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class HistoryOperationService {

    @Autowired
    HistoryOperationRepository historyOperationRepository;

    private Long userId = null;

    private Long objectId = null;

    private JSONObject before = new JSONObject();

    private JSONObject after = new JSONObject();

    private ObjectType type = ObjectType.TRUSTED_DEVICE;

    private String description = "";

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public void setBefore(JSONObject obj) {
        this.before = obj == null ? new JSONObject() : obj;
    }

    public void setAfter(JSONObject obj) {
        this.after = obj == null ? new JSONObject() : obj;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void init(Long objectId, Long userId, JSONObject before, ObjectType type, String description) {
        this.setBefore(before);
        this.setType(type);
        this.setDescription(description);
        this.setUserId(userId);
        this.setObjectId(objectId);
    }

    public HistoryOperationService() {
        this.setBefore(null);
        this.setType(ObjectType.TRUSTED_DEVICE);
        this.setDescription("");
        this.setUserId(null);
        this.setObjectId(null);

    }

    public Page<HistoryOperation> getUserHistory(Long userId, Pageable pageable) {
        return historyOperationRepository.findAllByUserId(userId, pageable);
    }

    public Page<iHistoryOperationProjection> getUserHistoryForUser(Long userId, Pageable pageable) {
        return historyOperationRepository.findAllByUserIdForUser(userId, pageable);
    }

    public Page<HistoryOperation> getObjectHistory(Long objectId, Pageable pageable) {
        return historyOperationRepository.findAllByObjectId(objectId, pageable);
    }

    @Async
    public void store() {

        Runnable task = () -> {
            try {
                historyOperationRepository.save(new HistoryOperation(
                        this.objectId,
                        this.userId,
                        this.type,
                        this.description,
                        this.before,
                        this.after));

                this.setBefore(null);
                this.setType(ObjectType.TRUSTED_DEVICE);
                this.setDescription("");
                this.setUserId(null);
                this.setObjectId(null);

            } catch (Exception ex) {

            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    @Async
    public void store(JSONObject after) {
        this.setAfter(after);

        Runnable task = () -> {
            try {
                historyOperationRepository.save(new HistoryOperation(
                        this.objectId,
                        this.userId,
                        this.type,
                        this.description,
                        this.before,
                        this.after));

                this.setBefore(null);
                this.setType(ObjectType.TRUSTED_DEVICE);
                this.setDescription("");
                this.setUserId(null);
                this.setObjectId(null);

            } catch (Exception ex) {

            }
        };

        Thread thread = new Thread(task);

        thread.start();

    }

    public Page<HistoryOperation> getAllHistoryOperations(Pageable pageble) {
        return historyOperationRepository.findAll(pageble);
    }

}
