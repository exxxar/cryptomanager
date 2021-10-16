/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.services;

import com.core.cryptolib.enums.ObjectType;
import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.projections.iHistoryOperationProjection;
import com.trustedsolutions.cryptographic.model.HistoryOperation;
import com.trustedsolutions.cryptographic.model.User;

import com.trustedsolutions.cryptographic.repository.HistoryOperationRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import com.trustedsolutions.cryptographic.security.UserPrincipal;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class HistoryOperationService {

    @Autowired
    HistoryOperationRepository historyOperationRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<HistoryOperation> getUserHistory(Long userId, Pageable pageable) {
        return historyOperationRepository.findAllByUserId(userId, pageable);
    }

    public Page<iHistoryOperationProjection> getUserHistoryForUser(Long userId, Pageable pageable) {
        return historyOperationRepository.findAllByUserIdForUser(userId, pageable);
    }

    public Page<HistoryOperation> getObjectHistory(Long objectId, Pageable pageable) {
        return historyOperationRepository.findAllByObjectId(objectId, pageable);
    }

    public void store(String description, Long objectId, JSONObject stateBefore, JSONObject stateAfter, ObjectType type) {

        JSONObject tmpAfter = stateAfter == null ? new JSONObject() : stateAfter;
        JSONObject tmpBefore = stateBefore == null ? new JSONObject() : stateBefore;

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = ((UserPrincipal) principal).getId();

        historyOperationRepository.save(new HistoryOperation(
                objectId,
                id,
                type,
                description,
                tmpAfter,
                tmpBefore));

    }

    public Page<HistoryOperation> getAllHistoryOperations(Pageable pageble) {
        return historyOperationRepository.findAll(pageble);
    }

}
