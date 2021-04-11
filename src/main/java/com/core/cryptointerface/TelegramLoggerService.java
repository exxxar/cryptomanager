/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.cryptointerface;

/**
 *
 * @author SAMS
 */
public class TelegramLoggerService extends com.core.cryptolib.TelegramLoggerService {

    public TelegramLoggerService(String channel, String token, String appName, String className, Boolean isDebug) {
        super(channel, token, appName, className, isDebug);
    }
}
