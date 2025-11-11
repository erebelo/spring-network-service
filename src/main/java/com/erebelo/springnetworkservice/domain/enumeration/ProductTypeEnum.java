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
public enum ProductTypeEnum implements EnumValueType {

    @JsonProperty("Life Insurance")
    LIFE_INSURANCE("Life Insurance"),

    @JsonProperty("Health Insurance")
    HEALTH_INSURANCE("Health Insurance"),

    @JsonProperty("Pension")
    PENSION("Pension"),

    @JsonProperty("Investment")
    INVESTMENT("Investment");

    private final String value;

    private static final Map<String, ProductTypeEnum> ENUM_MAP;

    static {
        Map<String, ProductTypeEnum> map = new HashMap<>();
        for (ProductTypeEnum instance : ProductTypeEnum.values()) {
            map.put(instance.getValue(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static ProductTypeEnum fromValue(String value) {
        return ENUM_MAP.get(value);
    }
}
