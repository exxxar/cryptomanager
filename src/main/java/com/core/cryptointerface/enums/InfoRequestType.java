/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.cryptointerface.enums;

/**
 *
 * @author SAMS
 */
public enum InfoRequestType {
    
    trustedDevicePublicId(0),
    onceEncryptedRequest(1),
    twiceEncryptedRequest(2),
    onceEncryptedPermission(3),
    twiceEncryptedPermission(4),
    permission(5),

    /**
     *
     */
    denial(6),
    data(7),
    encryptedData(8);

    int value;

    InfoRequestType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
