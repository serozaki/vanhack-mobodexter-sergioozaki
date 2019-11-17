package com.serozaki.mobodexter.raspberryagent.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudConfigRepository extends JpaRepository<CloudConfig, Long> {

    CloudConfig findByNameAndType(String name, CloudType type);
}