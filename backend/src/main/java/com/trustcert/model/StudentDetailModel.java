package com.trustcert.model;

import lombok.Data;

@Data
public class StudentDetailModel {

    private String email;
    // TODO : make isVerified false once verification email implementation is in place
    private Boolean isVerified = Boolean.FALSE;

    public StudentDetailModel(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        this.isVerified = verified;
    }

}
