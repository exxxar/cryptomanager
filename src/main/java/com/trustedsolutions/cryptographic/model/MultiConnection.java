/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.model;

/**
 *
 * @author SAMS
 */
import com.core.cryptolib.forms.MultiConnectionForm;
import com.core.cryptolib.forms.TrustedDeviceForm;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trustedsolutions.cryptographic.listeners.CompanyListener;
import com.trustedsolutions.cryptographic.listeners.MultiConnectionListener;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.json.JSONException;
import org.json.simple.JSONObject;

@Entity
@EntityListeners(MultiConnectionListener.class)
public class MultiConnection implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

//    @Column(name = "trusted_device_recipient_id")
//    private Long trustedDeviceRecipientId;
//
//    @Column(name = "trusted_device_sender_id")
//    private Long trustedDeviceSenderId;
    @Column(name = "trusted_device_multiply_actual_key", nullable = true)
    private String trustedDeviceMultiplyActualKey;

    @Column(name = "trusted_device_multiply_old_key", nullable = true)
    private String trustedDeviceMultiplyOldKey;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    @Column(name = "create_date_time")
    private LocalDateTime createDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimestamp
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    private TrustedDevice tdSender;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    private TrustedDevice tdRecipient;

    public MultiConnection() {
    }

    public MultiConnection(MultiConnectionForm mcf) {
        if (mcf.getId() != null) {
            this.id = mcf.getId();
        }
//        this.tdRecipient = mcf.getTrustedDeviceRecipientId();
//        this.tdSender = mcf.getTrustedDeviceSenderId();
        this.trustedDeviceMultiplyActualKey = mcf.getTrustedDeviceMultiplyActualKey() == null ? "" : mcf.getTrustedDeviceMultiplyActualKey();
        this.trustedDeviceMultiplyOldKey = mcf.getTrustedDeviceMultiplyOldKey() == null ? "" : mcf.getTrustedDeviceMultiplyOldKey();
    }

    public MultiConnection(String trustedDeviceMultiplyActualKey, String trustedDeviceMultiplyOldKey) {
//        this.trustedDeviceRecipientId = trustedDeviceRecipientId;
//        this.trustedDeviceSenderId = trustedDeviceSenderId;
        this.trustedDeviceMultiplyActualKey = trustedDeviceMultiplyActualKey;
        this.trustedDeviceMultiplyOldKey = trustedDeviceMultiplyOldKey;
    }

    public void setMultiConnection(MultiConnectionForm mcForm) {

//        this.trustedDeviceRecipientId = mcForm.getTrustedDeviceRecipientId() == null ? this.trustedDeviceRecipientId : mcForm.getTrustedDeviceRecipientId();
//        this.trustedDeviceSenderId = mcForm.getTrustedDeviceSenderId() == null ? this.trustedDeviceSenderId : mcForm.getTrustedDeviceSenderId();
        this.trustedDeviceMultiplyActualKey = mcForm.getTrustedDeviceMultiplyActualKey() == null ? this.trustedDeviceMultiplyActualKey : mcForm.getTrustedDeviceMultiplyActualKey();
        this.trustedDeviceMultiplyOldKey = mcForm.getTrustedDeviceMultiplyOldKey() == null ? this.trustedDeviceMultiplyOldKey : mcForm.getTrustedDeviceMultiplyOldKey();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public Long getTrustedDeviceRecipientId() {
//        return trustedDeviceRecipientId;
//    }
//
//    public void setTrustedDeviceRecipientId(Long trustedDeviceRecipientId) {
//        this.trustedDeviceRecipientId = trustedDeviceRecipientId;
//    }
//
//    public Long getTrustedDeviceSenderId() {
//        return trustedDeviceSenderId;
//    }
//
//    public void setTrustedDeviceSenderId(Long trustedDeviceSenderId) {
//        this.trustedDeviceSenderId = trustedDeviceSenderId;
//    }
    public String getTrustedDeviceMultiplyActualKey() {
        return trustedDeviceMultiplyActualKey;
    }

    public void setTrustedDeviceMultiplyActualKey(String trustedDeviceMultiplyActualKey) {
        this.trustedDeviceMultiplyActualKey = trustedDeviceMultiplyActualKey;
    }

    public String getTrustedDeviceMultiplyOldKey() {
        return trustedDeviceMultiplyOldKey;
    }

    public void setTrustedDeviceMultiplyOldKey(String trustedDeviceMultiplyOldKey) {
        this.trustedDeviceMultiplyOldKey = trustedDeviceMultiplyOldKey;
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

    public TrustedDevice getTdSender() {
        return tdSender;
    }

    public void setTdSender(TrustedDevice tdSender) {
        this.tdSender = tdSender;
    }

    public TrustedDevice getTdRecipient() {
        return tdRecipient;
    }

    public void setTdRecipient(TrustedDevice tdRecipient) {
        this.tdRecipient = tdRecipient;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject mc = new JSONObject();
        mc.put("id", id);
        mc.put("trustedDeviceMultiplyActualKey", trustedDeviceMultiplyActualKey);
        mc.put("trustedDeviceMultiplyOldKey", trustedDeviceMultiplyOldKey);

        mc.put("createDateTime", createDateTime);
        mc.put("updateDateTime", updateDateTime);

        return mc;
    }

}
