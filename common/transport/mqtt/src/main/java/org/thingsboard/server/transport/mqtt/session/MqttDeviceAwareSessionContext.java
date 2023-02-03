/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.mqtt.session;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.thingsboard.server.common.transport.session.DeviceAwareSessionContext;
import org.thingsboard.server.gen.transport.mqtt.SparkplugBProto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by ashvayka on 30.08.18.
 */
public abstract class MqttDeviceAwareSessionContext extends DeviceAwareSessionContext {

    private final ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap;
    private Map<String, SparkplugBProto.Payload.Metric> deviceBirthMetrics;

    public MqttDeviceAwareSessionContext(UUID sessionId, ConcurrentMap<MqttTopicMatcher, Integer> mqttQoSMap) {
        super(sessionId);
        this.mqttQoSMap = mqttQoSMap;
        this.deviceBirthMetrics = null;
    }

    public ConcurrentMap<MqttTopicMatcher, Integer> getMqttQoSMap() {
        return mqttQoSMap;
    }

    public  Map<String, SparkplugBProto.Payload.Metric> getDeviceBirthMetrics() {
        return deviceBirthMetrics;
    }

    public void setDeviceBirthMetrics(java.util.List<org.thingsboard.server.gen.transport.mqtt.SparkplugBProto.Payload.Metric> metrics) {
        if  (this.deviceBirthMetrics == null) {
            this.deviceBirthMetrics = new ConcurrentHashMap<>();
        }
        this.deviceBirthMetrics.putAll(metrics.stream()
                .collect(Collectors.toMap(metric -> metric.getName(), metric -> metric)));
    }

    public MqttQoS getQoSForTopic(String topic) {
        List<Integer> qosList = mqttQoSMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches(topic))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (!qosList.isEmpty()) {
            return MqttQoS.valueOf(qosList.get(0));
        } else {
            return MqttQoS.AT_LEAST_ONCE;
        }
    }
}
