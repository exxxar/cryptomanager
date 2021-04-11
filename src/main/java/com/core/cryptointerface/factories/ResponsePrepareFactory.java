package com.core.cryptointerface.factories;

import com.core.cryptolib.TelegramLoggerService;
import org.json.simple.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

public class ResponsePrepareFactory extends JSONObject {
    
    TelegramLoggerService logger;
    
    public ResponsePrepareFactory(TelegramLoggerService logger) {
        this.logger = logger;
        this.logger.setClassName(ResponsePrepareFactory.class.getName());
    }
    
    public JSONObject responseMessageFormat(HttpStatus status, String messageKey, Object[] objs, MessageSource messageSource) {
        JSONObject message = new JSONObject();
        
        message.put("detail", messageSource.getMessage(messageKey,
                objs,
                LocaleContextHolder.getLocale()));
        message.put("title", status.name());
        message.put("code", status.value());
        
        logger.info(message.toJSONString());
        
        return message;
    }
    
    public JSONObject responseMessageFormat(HttpStatus status, String textMessage) {
        JSONObject message = new JSONObject();
        
        message.put("detail", textMessage);
        message.put("title", status.name());
        message.put("code", status.value());
        
        logger.info(message.toJSONString());
        
        return message;
    }
}
