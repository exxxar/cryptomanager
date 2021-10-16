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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


@EntityScan(basePackages = {"com.trustedsolutions.cryptographic.model"})
public interface CompanyRepository extends PagingAndSortingRepository<Company, Long> {

    Company findCompanyById(Long id);

    @Transactional
    @Modifying
    @Query("delete from Company c where c.id=:id")
    void deleteCompany(@Param("id") Long id);

    @Override
    Page<Company> findAll(Pageable p);

//    //@Query(value = "SELECT * FROM company WHERE id = :id or name LIKE %:name% or active = :active", nativeQuery = true)
//    Page<Company> findByNameContainingIgnoreCase(
//            @Param("name") String name,
//            Pageable p);
    @Query(value = "SELECT e FROM Company as e WHERE e.id=:id or (e.name LIKE %:inputString%) or (e.description LIKE %:inputString%)")
    Page<Company> findAllByInputString(Long id, @Param("inputString") String inputString, Pageable pageable);
}
