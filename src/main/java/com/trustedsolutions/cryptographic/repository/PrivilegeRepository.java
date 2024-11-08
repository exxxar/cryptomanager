/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.repository;

import com.trustedsolutions.cryptographic.model.Privilege;
import com.trustedsolutions.cryptographic.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author SAMS
 */

@Repository
public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {

    Privilege findByName(String name);
}