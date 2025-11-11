package com.erebelo.springnetworkservice.domain.enumeration;

import com.erebelo.springnetworkservice.domain.enumeration.type.EnumValueType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatusEnum implements EnumValueType {

    @JsonProperty("Active")
    ACTIVE("Active"),

    @JsonProperty("Inactive")
    INACTIVE("Inactive");

    private final String value;

    private static final Map<String, StatusEnum> ENUM_MAP;

    static {
        Map<String, StatusEnum> map = new HashMap<>();
        for (StatusEnum instance : StatusEnum.values()) {
            map.put(instance.getValue(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static StatusEnum fromValue(String value) {
        return ENUM_MAP.get(value);
    }
}
