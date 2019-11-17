package com.serozaki.mobodexter.raspberryagent.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "cloud_config")
public class CloudConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @Enumerated(EnumType.STRING)
    private CloudType type;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_cloud_config")
    private Set<CloudConfigProperty> properties;

    public String findStringProperty(String key, String defaultValue) {
        if (getProperties() != null) {
            return getProperties().stream().filter(p -> key.equalsIgnoreCase(p.getName()))
                    .map(CloudConfigProperty::getValue).findFirst().orElse(defaultValue);
        }
        return defaultValue;
    }

    public Integer findIntProperty(String key, Integer defaultValue) {
        try {
            String stringValue = findStringProperty(key, null);
            return stringValue != null ? Integer.parseInt(stringValue) : defaultValue;
        } catch (NumberFormatException e) {
            LoggerFactory.getLogger(CloudConfig.class).error(e.getMessage(), e);
            return defaultValue;
        }
    }
}
