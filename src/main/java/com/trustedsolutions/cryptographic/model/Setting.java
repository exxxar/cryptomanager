package com.trustedsolutions.cryptographic.model;

import com.core.cryptolib.enums.TypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trustedsolutions.cryptographic.listeners.MultiConnectionListener;
import com.trustedsolutions.cryptographic.listeners.SettingsListener;
import java.io.Serializable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import org.json.JSONException;
import org.json.simple.JSONObject;

@Entity
@Table(name = "Settings")
@EntityListeners(SettingsListener.class)
public class Setting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(length = 512, columnDefinition = "varchar(255) default ''", unique = true)
    private String settingKey;

    @Column(length = 512, columnDefinition = "varchar(255) default ''")
    private String settingValue;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TypeEnum type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    public Setting() {

    }

    public Setting(String key, String value) {
        this.settingKey = key;
        this.settingValue = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return settingKey;
    }

    public void setKey(String key) {
        this.settingKey = key;
    }

    public String getValue() {
        return settingValue;
    }

    public void setValue(String value) {
        this.settingValue = value;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject settings = new JSONObject();
        settings.put("id", id);
        settings.put("settingKey", settingKey);
        settings.put("settingValue", settingValue);
        settings.put("type", type);
        settings.put("createDateTime", createDateTime);
        settings.put("updateDateTime", updateDateTime);

        return settings;
    }
}
