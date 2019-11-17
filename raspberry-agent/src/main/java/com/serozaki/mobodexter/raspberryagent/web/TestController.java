package com.serozaki.mobodexter.raspberryagent.web;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;
import com.serozaki.mobodexter.raspberryagent.model.CloudConfigRepository;
import com.serozaki.mobodexter.raspberryagent.service.IotClient;
import com.serozaki.mobodexter.raspberryagent.service.IotClientService;

@RestController
@RequestMapping("/api/test")
class TestController {

    private Logger log = LoggerFactory.getLogger(TestController.class);
    private CloudConfigRepository cloudConfigRepository;
    private IotClientService iotClientService;

    public TestController(CloudConfigRepository cloudConfigRepository, IotClientService iotClientService) {
        this.cloudConfigRepository = cloudConfigRepository;
        this.iotClientService = iotClientService;
    }

    @PostMapping("/publishTest/{id}")
    public ResponseEntity<?> publishTest(@PathVariable Long id, @RequestBody String message) throws URISyntaxException {
        log.info("publishTest: {}", id);
        CloudConfig cloudConfig = cloudConfigRepository.getOne(id);
        IotClient client = iotClientService.newClient(cloudConfig);
        try {
            client.connect();
            client.publish(1, message);
            Thread.sleep(5000);
            client.disconnect();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}