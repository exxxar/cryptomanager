/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.forms.iHistoryOperationProjection;
import com.trustedsolutions.cryptographic.model.HistoryOperation;
import org.json.simple.JSONObject;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface HistoryOperationRepository extends PagingAndSortingRepository<HistoryOperation, Long> {

    HistoryOperation findHistoryOperationById(Long id);

    @Query(value = "SELECT *  FROM `history_operation` WHERE `history_operation`.`user_id`=:userId", nativeQuery = true)
    Page<HistoryOperation> findAllByUserId(@Param("userId") Long userId, Pageable p);

    @Query(value = "SELECT `id`,`create_date_time`,`update_date_time`,`description`, `object_id`,`object_type`,`user_id` FROM `history_operation` WHERE `history_operation`.`user_id`=:userId", nativeQuery = true)
    Page<iHistoryOperationProjection> findAllByUserIdForUser(@Param("userId") Long userId, Pageable p);

    @Query(value = "SELECT *  FROM `history_operation` WHERE `history_operation`.`object_id`=:objectId", nativeQuery = true)
    Page<HistoryOperation> findAllByObjectId(@Param("objectId") Long objectId, Pageable p);

    @Override
    Page<HistoryOperation> findAll(Pageable p);

}
