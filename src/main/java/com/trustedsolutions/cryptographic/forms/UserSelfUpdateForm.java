/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions.cryptographic.forms;

import javax.validation.constraints.NotNull;

/**
 *
 * @author SAMS
 */
public class UserSelfUpdateForm {

    private String name;

    private String email;

    private String imageUrl;

    private Boolean enabled;

    private String password;

    private String confirmPassword;

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setPassword(String password) {
        this.password = password;
        checkPassword();//check
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        checkPassword();//check
    }

    private void checkPassword() {
        if (this.password == null || this.confirmPassword == null) {
            return;
        } else if (!this.password.equals(confirmPassword)) {
            this.confirmPassword = null;
        }
    }

    public UserSelfUpdateForm(String name, String imageUrl, Boolean enabled, String password) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.enabled = enabled;
        this.password = password;
    }

    public UserSelfUpdateForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

}
