package com.serozaki.mobodexter.raspberryagent.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;
import com.serozaki.mobodexter.raspberryagent.model.CloudConfigRepository;

@RestController
@RequestMapping("/api")
class ConfigController {

    private Logger log = LoggerFactory.getLogger(ConfigController.class);
    private CloudConfigRepository cloudConfigRepository;

    public ConfigController(CloudConfigRepository cloudConfigRepository) {
        this.cloudConfigRepository = cloudConfigRepository;
    }

    @GetMapping("/configs")
    public Collection<CloudConfig> configs() {
        return cloudConfigRepository.findAll();
    }

    @GetMapping("/config/{id}")
    public ResponseEntity<?> getConfig(@PathVariable Long id) {
        Optional<CloudConfig> group = cloudConfigRepository.findById(id);
        return group.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/config")
    public ResponseEntity<CloudConfig> createConfig(@Valid @RequestBody CloudConfig cloudConfig)
            throws URISyntaxException {
        log.info("createGroup: {}", cloudConfig);
        CloudConfig result = cloudConfigRepository.save(cloudConfig);
        return ResponseEntity.created(new URI("/api/group/" + result.getId())).body(result);
    }

    @PutMapping("/config/{id}")
    public ResponseEntity<CloudConfig> updateConfig(@Valid @RequestBody CloudConfig cloudConfig) {
        log.info("updateConfig: {}", cloudConfig);
        CloudConfig result = cloudConfigRepository.save(cloudConfig);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/config/{id}")
    public ResponseEntity<?> deleteConfig(@PathVariable Long id) {
        log.info("deleteConfig: {}", id);
        cloudConfigRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}