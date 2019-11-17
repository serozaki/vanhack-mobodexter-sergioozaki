package com.serozaki.mobodexter.raspberryagent.service;

import java.io.IOException;

import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;

public interface IotClient {

    void configure(CloudConfig cloudConfig);

    void connect() throws IOException;

    void disconnect() throws IOException;

    void publish(int sensorId, String message) throws IOException;

}
