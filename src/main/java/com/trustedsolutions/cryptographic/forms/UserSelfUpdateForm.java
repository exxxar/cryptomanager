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

   


    public UserSelfUpdateForm(String name, String imageUrl, Boolean enabled) {
        this.name = name;
        this.imageUrl = imageUrl;
     
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

  

}
