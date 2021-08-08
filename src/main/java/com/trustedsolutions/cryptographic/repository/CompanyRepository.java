/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.Company;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface CompanyRepository extends PagingAndSortingRepository<Company, Long> {

    Company findCompanyById(Long id);

    @Override
    Page<Company> findAll(Pageable p);

}
