package com.serozaki.mobodexter.raspberryagent.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "cloud_config_property")
public class CloudConfigProperty {
    @Id
    @GeneratedValue
    private Long id;
//    @Column(name = "fk_cloud_config")
//    @ToString.Exclude
//    private Long configId;
    @NonNull
    private String name;
    @Lob
    @NonNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String value;
}
