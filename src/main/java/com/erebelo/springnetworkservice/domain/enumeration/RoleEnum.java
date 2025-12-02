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
public enum RoleEnum implements EnumValueType {

    @JsonProperty("Insurer")
    INSURER("Insurer"),

    @JsonProperty("Agency")
    AGENCY("Agency"),

    @JsonProperty("Regional Manager")
    REGIONAL_MANAGER("Regional Manager"),

    @JsonProperty("Agent")
    AGENT("Agent"),

    @JsonProperty("Sub-Agent")
    SUB_AGENT("Sub-Agent"),

    @JsonProperty("Advisor")
    ADVISOR("Advisor"),

    @JsonProperty("Individual")
    INDIVIDUAL("Individual"),

    @JsonProperty("Policy Holder")
    POLICY_HOLDER("Policy Holder"),

    @JsonProperty("Dependent")
    DEPENDENT("Dependent"),

    @JsonProperty("Beneficiary")
    BENEFICIARY("Beneficiary");

    private final String value;

    private static final Map<String, RoleEnum> ENUM_MAP;

    static {
        Map<String, RoleEnum> map = new HashMap<>();
        for (RoleEnum instance : RoleEnum.values()) {
            map.put(instance.getValue(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static RoleEnum fromValue(String value) {
        return ENUM_MAP.get(value);
    }
}
