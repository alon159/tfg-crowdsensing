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
 * Source to get data from android step counter sensor.
 */
@Extension(
        name = "android-steps",
        namespace = "source",
        description = "Steps Source gets events from step detector sensor of android device " +
                "which is a software based sensor.",
        parameters = {
                @Parameter(
                        name = "polling.interval",
                        description = " polling.interval is the time between two events in " +
                                "milliseconds. " +
                                "If a polling interval is specified events are generated only at " +
                                "that frequency even if the sensor value changes. If this is not " +
                                "specified 1 will be given as output for each step taken. " +
                                "Otherwise number of steps taken within this interval",
                        defaultValue = "0L",
                        optional = true,
                        type = {DataType.LONG}
                )
        },
        examples = {
                @Example(
                        syntax = "@source(type = 'android-steps' ,polling.interval = 100 ," +
                                "@map(type='keyvalue'))\n" +
                                "define stream fooStream(sensor string, steps int, accuracy float)",
                        description = "This will consume events from Step Detector sensor" +
                                " transport " +
                                "It will insert number of steps taken within the time frame of" +
                                " polling interval.\n"
                ),
                @Example(
                        syntax = "@source(type = 'android-steps' ,@map(type='keyvalue'))\n" +
                                "define stream fooStream(sensor string, steps int, accuracy float)",
                        description = "This will consume events from Proximity sensor transport " +
                                "When ever user took a step it will input an event into the " +
                                "stream which has steps count as 1\n"
                )
        }
)
public class StepCounterSensoorSource extends AbstractSensorSource {
    private int stepCount = 0;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] strings, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        super.init(sourceEventListener, optionHolder, strings, configReader, siddhiAppContext);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (sensor == null) {
            throw new SiddhiAppCreationException("Step Counter Sensor is not supported in the " +
                    "device. Stream " + sourceEventListener.getStreamDefinition().getId() +
                    ", App : " + siddhiAppContext.getName());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            stepCount++;
            Map<String, Object> output = new HashMap<>();
            output.put("sensor", event.sensor.getName());
            output.put("timestamp", event.timestamp);
            output.put("accuracy", event.accuracy);
            output.put("steps", stepCount);
            if (this.pollingInterval == 0L) {
                this.sourceEventListener.onEvent(output, null);
                stepCount = 0;
            }
            this.latestInput = output;
        }
    }

    protected void postUpdates() {
        super.postUpdates();
        this.stepCount = 0;
    }
}
