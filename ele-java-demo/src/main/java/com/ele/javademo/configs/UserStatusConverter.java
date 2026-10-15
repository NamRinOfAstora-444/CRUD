package com.ele.javademo.configs;

import com.ele.javademo.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer>{
    @Override
    public Integer convertToDatabaseColumn(UserStatus status) {
        if (status == null) return null;
        return status.getDbValue();
    }

    @Override
    public UserStatus convertToEntityAttribute(Integer value) {
        if (value == null) return null;
        return UserStatus.fromDbValue(value);
    }
}
