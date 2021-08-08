/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.HistoryOperation;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface HistoryOperationRepository extends PagingAndSortingRepository<HistoryOperation, Long> {

    HistoryOperation findHistoryOperationById(Long id);

    @Override
    Page<HistoryOperation> findAll(Pageable p);

}
