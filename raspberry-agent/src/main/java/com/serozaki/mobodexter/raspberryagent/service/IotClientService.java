package com.serozaki.mobodexter.raspberryagent.service;

import org.springframework.stereotype.Service;

import com.serozaki.mobodexter.raspberryagent.model.CloudConfig;
import com.serozaki.mobodexter.raspberryagent.model.CloudType;

@Service
public class IotClientService {

    public IotClient newClient(CloudConfig cloudConfig) {
        IotClient client = null;
        if (cloudConfig.getType() == CloudType.AWS) {
            client = new AwsIotClient();
            client.configure(cloudConfig);
            return client;
        }
        if (cloudConfig.getType() == CloudType.GOOGLE_CLOUD) {
            client = new GoogleIotClient();
            client.configure(cloudConfig);
            return client;
        }
        throw new IllegalArgumentException("Invalid cloud");
    };

}
