package com.ele.javademo.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE", 1),
    INACTIVE("INACTIVE", 0);

    private final String value;
    private final int dbValue;

    UserStatus(String value, int dbValue) {
        this.value = value;
        this.dbValue = dbValue;
    }

    @JsonValue
    public  String getValue() {
        return value;
    }

    public static  UserStatus fromDbValue(int dbValue) {
        for (UserStatus status : values()) {
            if (status.dbValue == dbValue) return status;
        }
        throw new IllegalArgumentException("Unknown status db value: " + dbValue);
    }
}
