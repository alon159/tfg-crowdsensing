/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.io.android.source.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to get data from temperature sensor.
 */
@Extension(
        name = "android-temperature",
        namespace = "source",
        description = "Temperature Source gets events from temperature sensor of android device." +
                "This will output a key value map with temperature (temperature in C)," +
                " sensor (sensor name), accuracy(could be HIGH (3) ,MEDIUM (2) or LOW (1) ) " +
                "and timestamp(time at which event arrives) as keys",
        parameters = {
                @Parameter(
                        name = "polling.interval",
                        description = " polling.interval is the time between two events in " +
                                "milliseconds. If a polling interval is specified events " +
                                "are generated only at " +
                                "that frequency even if the sensor value changes.",
                        defaultValue = "0L",
                        optional = true,
                        type = {DataType.LONG}
                )
        },
        examples = {
                @Example(
                        syntax = "@source(type = 'android-temperature' ,@map(type='keyvalue'))\n" +
                                "define stream temperatureStream(sensor string," +
                                " temperature float, accuracy int)",
                        description = "This will consume events from Temperature sensor" +
                                " transport " +
                                "when the sensor value is changed.\n"
                ),
                @Example(
                        syntax = "@source(type = 'android-temperature' ,polling.interval = 100," +
                                "@map(type='keyvalue'))\n" +
                                "define stream temperatureStream(sensor string," +
                                " temperature float, accuracy int)",
                        description = "This will consume events from Temperature sensor " +
                                "transport " +
                                "periodically with a interval of 100 milliseconds.\n"
                )
        }
)
public class TemperatureSensorSource extends AbstractSensorSource {

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] strings, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        super.init(sourceEventListener, optionHolder, strings, configReader, siddhiAppContext);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensor == null) {
            throw new SiddhiAppCreationException("Temperature Sensor is not supported in the " +
                    "device. Stream " + sourceEventListener.getStreamDefinition().getId() +
                    ", App : " + siddhiAppContext.getName());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Map<String, Object> output = new HashMap<>();
        output.put("sensor", event.sensor.getName());
        output.put("timestamp", event.timestamp);
        output.put("accuracy", event.accuracy);
        output.put("temperature", event.values[0]);
        if (this.pollingInterval == 0L && (this.latestInput == null ||
                (float) this.latestInput.get("temperature") != (float) output.get("temperature"))) {
            this.sourceEventListener.onEvent(output, null);
        }
        this.latestInput = output;
    }
}
