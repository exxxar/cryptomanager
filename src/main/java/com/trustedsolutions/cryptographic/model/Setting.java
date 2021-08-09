package com.trustedsolutions.cryptographic.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Table;

@Entity
@Table(name = "Settings")
public class Setting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(length = 512, columnDefinition = "varchar(255) default ''", unique = true)
    private String settingKey;

    @Column(length = 512, columnDefinition = "varchar(255) default ''")
    private String settingValue;

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

}
