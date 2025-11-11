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
public enum BusinessChannelEnum implements EnumValueType {

    @JsonProperty("Direct")
    DIRECT("Direct"),

    @JsonProperty("Digital")
    DIGITAL("Digital"),

    @JsonProperty("Banking")
    BANKING("Banking"),

    @JsonProperty("Broker")
    BROKER("Broker"),

    @JsonProperty("Affinity")
    AFFINITY("Affinity");

    private final String value;

    private static final Map<String, BusinessChannelEnum> ENUM_MAP;

    static {
        Map<String, BusinessChannelEnum> map = new HashMap<>();
        for (BusinessChannelEnum instance : BusinessChannelEnum.values()) {
            map.put(instance.getValue(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static BusinessChannelEnum fromValue(String value) {
        return ENUM_MAP.get(value);
    }
}
