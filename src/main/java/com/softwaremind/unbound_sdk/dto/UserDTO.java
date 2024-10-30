package com.softwaremind.unbound_sdk.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class UserDTO {

    private String baseDN;

    private String uniqueName;

    private String phoneNumber;

    private String fname;

    private String lname;

    private String address;

    @Override
    public String toString() {
        return "UserDTO{" +
                "baseDN='" + baseDN + '\'' +
                ", uniqueName='" + uniqueName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
