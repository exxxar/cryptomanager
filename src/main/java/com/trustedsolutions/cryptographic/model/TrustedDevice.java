package com.trustedsolutions.cryptographic.model;

import com.core.cryptolib.forms.TrustedDeviceForm;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trustedsolutions.cryptographic.services.SettingsService;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Formula;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
public class TrustedDevice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "device_public_id", unique = true)
    private String devicePublicId;

    @Column(name = "device_reset_key")
    private String deviceResetKey;

    @Column(name = "device_factory_key")
    private String deviceFactoryKey;

    @Column(name = "device_private_id", unique = true)
    private String devicePrivateId;

    @Column(name = "device_actual_key")
    private String deviceActualKey;

    @Column(name = "device_old_key")
    private String deviceOldKey;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_update_actual_key_date_time", nullable = true)
    private LocalDateTime lastUpdateActualKeyDateTime;

    @Column(name = "active", columnDefinition = "boolean default false")
    private boolean active;

    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    @Column(name = "attempts", columnDefinition = "integer default 0")
    private int attempts;

    @Column(name = "current_firmware", columnDefinition = "")
    private String currentFirmware;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    @Column(name = "create_date_time")
    private LocalDateTime createDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimestamp
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

    @ManyToMany(mappedBy = "trustedDevices")
    private Set<Company> companies;

//    @Formula(value = "(select t.setting_value from settings t where t.setting_key = 'actualFirmware')")
//    private String actualFirmware;
//
//    public boolean getActualFirmware() {
//
//        if (currentFirmware == null || actualFirmware == null) {
//            return false;
//        }
//
//        return this.currentFirmware.equals(actualFirmware);
//    }

    public TrustedDevice(TrustedDeviceForm tdForm) {
        setTrustedDevice(tdForm);
    }

    public void setTrustedDevice(TrustedDeviceForm tdForm) {

        this.devicePublicId = tdForm.getDevicePublicId() == null ? this.devicePublicId : tdForm.getDevicePublicId();
        this.devicePrivateId = tdForm.getDevicePrivateId() == null ? this.devicePrivateId : tdForm.getDevicePrivateId();

        this.setDeviceActualKey(tdForm.getDeviceActualKey());
        this.setDeviceOldKey(tdForm.getDeviceOldKey());
        this.setDeviceResetKey(tdForm.getDeviceResetKey());
        this.setDeviceFactoryKey(tdForm.getDeviceFactoryKey());
        this.setAttempts(tdForm.getAttempts());

        this.active = tdForm.isActive();
        this.description = tdForm.getDescription() == null ? this.description : tdForm.getDescription();

    }

    public TrustedDevice(String devicePublicId,
            String devicePrivateId,
            byte[] deviceActualKey,
            byte[] deviceOldKey,
            byte[] deviceResetKey,
            byte[] deviceFactoryKey,
            boolean active,
            String description) {
        this.devicePublicId = devicePublicId;
        this.devicePrivateId = devicePrivateId;
        this.setDeviceActualKeyEncode(deviceActualKey);
        this.setDeviceOldKeyEncode(deviceOldKey);
        this.setDeviceResetKeyEncode(deviceResetKey);
        this.setDeviceFactoryKeyEncode(deviceFactoryKey);
        this.active = active;
        this.attempts = 0;
        this.description = description;
        this.currentFirmware = "";
    }

    public TrustedDevice(String devicePublicId,
            String devicePrivateId,
            String deviceActualKey,
            String deviceOldKey,
            String deviceResetKey,
            String deviceFactoryKey,
            boolean active,
            String description) {
        this.devicePublicId = devicePublicId;
        this.devicePrivateId = devicePrivateId;
        this.setDeviceActualKey(deviceActualKey);
        this.setDeviceOldKey(deviceOldKey);
        this.setDeviceResetKey(deviceResetKey);
        this.setDeviceFactoryKey(deviceFactoryKey);
        this.description = description;
        this.attempts = 0;
        this.currentFirmware = "";

    }

    public TrustedDevice() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDevicePublicId(String devicePublicId) {
        this.devicePublicId = devicePublicId;
    }

    public void setDevicePrivateId(String devicePrivateId) {
        this.devicePrivateId = devicePrivateId;
    }

    public void setDeviceResetKey(String deviceResetKey) {
        this.deviceResetKey = deviceResetKey;
    }

    public void setDeviceActualKey(String deviceActualKey) {
        this.deviceActualKey = deviceActualKey;
    }

    public void setDeviceOldKey(String deviceOldKey) {
        this.deviceOldKey = deviceOldKey;
    }

    public void setDeviceResetKeyEncode(byte[] deviceResetKey) {
        this.deviceResetKey = Base64.getEncoder().encodeToString(deviceResetKey);
    }

    public void setDeviceFactoryKeyEncode(byte[] deviceFactoryKey) {
        this.deviceFactoryKey = Base64.getEncoder().encodeToString(deviceFactoryKey);
    }

    public void setDeviceActualKeyEncode(byte[] deviceActualKey) {
        this.deviceActualKey = Base64.getEncoder().encodeToString(deviceActualKey);
    }

    public void setDeviceOldKeyEncode(byte[] deviceOldKey) {
        this.deviceOldKey = Base64.getEncoder().encodeToString(deviceOldKey);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateDateTime() {
        return this.createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return this.updateDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getDevicePublicId() {
        return devicePublicId;
    }

    public String getDevicePrivateId() {
        return devicePrivateId;
    }

    public byte[] getDeviceActualKey() {
        try {
            return Base64.getDecoder().decode(deviceActualKey.getBytes());
        } catch (Exception ex) {
            System.out.print("getDeviceActualKey=>" + ex.getMessage());
            return deviceActualKey.getBytes();
        }

    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public String getCurrentFirmware() {
        return currentFirmware;
    }

    public void setCurrentFirmware(String currentFirmware) {
        this.currentFirmware = currentFirmware;
    }

    public byte[] getDeviceResetKey() {

        try {
            return Base64.getDecoder().decode(deviceResetKey.getBytes());
        } catch (Exception ex) {

            return deviceResetKey.getBytes();
        }

    }

    public byte[] getDeviceFactoryKey() {

        try {
            return Base64.getDecoder().decode(deviceFactoryKey.getBytes());
        } catch (Exception ex) {

            return deviceResetKey.getBytes();
        }

    }

    public byte[] getDeviceOldKey() {
        try {
            return Base64.getDecoder().decode(deviceOldKey.getBytes());
        } catch (Exception ex) {

            return deviceOldKey.getBytes();
        }
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject device = new JSONObject();
        device.put("id", id);
        device.put("description", description);
        device.put("deviceActualKey", deviceActualKey);
        device.put("attempts", attempts);
        device.put("actualFirmware", currentFirmware);
        device.put("deviceResetKey", deviceResetKey);
        device.put("deviceFactoryKey", deviceFactoryKey);
        device.put("deviceOldKey", deviceOldKey);
        device.put("lastUpdateActualKeyDateTime", lastUpdateActualKeyDateTime);
        device.put("devicePrivateId", devicePrivateId);
        device.put("devicePublicId", devicePublicId);
        device.put("createDateTime", createDateTime);

        return device;
    }

    public TrustedDeviceForm toTrustedDeviceForm() {
        TrustedDeviceForm tdf = new TrustedDeviceForm();
        tdf.setId(id);
        tdf.setActive(active);
        tdf.setDescription(description);
        tdf.setCurrentFirmware(currentFirmware);
        tdf.setDeviceActualKey(deviceActualKey);
        tdf.setDeviceResetKey(deviceResetKey);
        tdf.setDeviceFactoryKey(deviceFactoryKey);
        tdf.setDeviceOldKey(deviceOldKey);
        tdf.setDevicePrivateId(devicePrivateId);
        tdf.setDevicePublicId(devicePublicId);
        tdf.setAttempts(attempts);

        return tdf;
    }

    public void setDeviceFactoryKey(String deviceFactoryKey) {
        this.deviceFactoryKey = deviceFactoryKey;
    }

    public LocalDateTime getLastUpdateActualKeyDateTime() {
        return lastUpdateActualKeyDateTime;
    }

    public void setLastUpdateActualKeyDateTime(LocalDateTime lastUpdateActualKeyDateTime) {
        this.lastUpdateActualKeyDateTime = lastUpdateActualKeyDateTime;
    }

}
