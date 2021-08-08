/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.services.storage;

/**
 *
 * @author SAMS
 */
public class StorageException extends RuntimeException  {
    public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
