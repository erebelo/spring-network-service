package com.erebelo.springnetworkservice.config;

import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "network")
public class NetworkProperties {

    @Getter
    private final Set<RoleEnum> rootCriteria;

    public NetworkProperties(List<String> validCriteria) {
        if (validCriteria == null || validCriteria.isEmpty()) {
            throw new IllegalStateException(
                    "Failed to initialize rootCriteria: 'network.valid-criteria' is missing or empty");
        }

        this.rootCriteria = validCriteria.stream().map(String::trim).map(RoleEnum::fromValue)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RoleEnum.class)));
    }
}
