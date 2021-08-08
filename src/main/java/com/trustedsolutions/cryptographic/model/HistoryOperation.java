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
import com.core.cryptolib.HashMapConverter;
import com.core.cryptolib.enums.ObjectType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trustedsolutions.cryptographic.forms.CompanyForm;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Entity
public class HistoryOperation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(nullable = true, name = "state_before")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> stateBefore = new JSONObject();

    @Column(nullable = true, name = "state_after")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> stateAfter = new JSONObject();

    @Column(name = "object_id", nullable = true)
    private Long objectId;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    private ObjectType objectType;

    @Column(name = "description", columnDefinition = "varchar(255) default ''")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    @Column(name = "create_date_time")
    private LocalDateTime createDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimestamp
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

//
//    public JSONObject toJSON() throws JSONException {
//        JSONObject company = new JSONObject();
//        company.put("id", getId());
//        company.put("active", isActive());
//        company.put("companyName", getCompanyName());
//        company.put("createDateTime", getCreateDateTime());
//        company.put("userCheckUrl", getUserCheckUrl());
//
//        JSONArray arr = new JSONArray();
//
//        if (getTrustedDevices() != null) {
//            for (TrustedDevice td : getTrustedDevices()) {
//                arr.add(td.toJSON());
//            }
//        }
//
//        company.put("trustedDevices", arr);
//
//        return company;
//    }

    public HistoryOperation(Long objectId, Long userId, ObjectType objectType, String description, JSONObject before, JSONObject after) {
        this.objectId = objectId;
        this.userId = userId;
        this.objectType = objectType;
        this.description = description;
        this.stateBefore = before;
        this.stateAfter = after;
    }
    
    
    
    public HistoryOperation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getStateBefore() {
        return stateBefore;
    }

    public void setStateBefore(Map<String, Object> stateBefore) {
        this.stateBefore = stateBefore;
    }

    public Map<String, Object> getStateAfter() {
        return stateAfter;
    }

    public void setStateAfter(Map<String, Object> stateAfter) {
        this.stateAfter = stateAfter;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
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
