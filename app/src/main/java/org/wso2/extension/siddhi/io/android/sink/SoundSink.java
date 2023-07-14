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
package org.wso2.extension.siddhi.io.android.sink;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import org.wso2.siddhi.android.platform.SiddhiAppService;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Sink to create notification sound.
 */
@Extension(
        name = "android-sound",
        namespace = "sink",
        description = "This will play android phone ringtone when ever it receives an event. " +
                "It will played for a user specified time period",
        parameters = {
                @Parameter(
                        name = "play.time",
                        description = "This parameter will specify the time period in seconds the "
                                + "ringing tone played",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "10"
                ),
                @Parameter(
                        name = "ring.tone.uri",
                        description = "This parameter will specify URI of the ringing tone to" +
                                " be played",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "Notification ring tone URI"
                )
        },
        examples = {
                @Example(
                        syntax = "@sink(type = 'android-sound',play.time = ‘10’, ring.tone='3'," +
                                "@map(type='keyvalue'))\n"
                                + "define stream fooStream(sensor string, value float," +
                                " accuracy float)",
                        description = "This will ring the device when ever it receives an event" +
                                "for 10 seconds time"
                )

        }
)

public class SoundSink extends Sink {

    public static final String TIME_STRING = "play.time";
    public static final String RING_TONE_URI_STRING = "ring.tone.uri";
    private int time = 10;
    private Uri uri;

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        time = Integer.parseInt(optionHolder.validateAndGetStaticValue(TIME_STRING,
                "10")) * 1000;
        if (time <= 0) {
            throw new SiddhiAppCreationException("Given play.time is invalid must be an integer" +
                    " greater than zero: "
                    + "Stream : " + streamDefinition.getId()
                    + "App: " + siddhiAppContext.getName());
        }
        String uriStr = optionHolder.validateAndGetStaticValue(RING_TONE_URI_STRING, "");
        if (uriStr.equals("")) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            uri = Uri.parse(uriStr);
        }

    }

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{Map.class, String.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }

    @Override
    public void publish(Object o, DynamicOptions dynamicOptions)
            throws ConnectionUnavailableException {
        playRingTone(uri, time);
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void destroy() {
        uri = null;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
    }

    private void playRingTone(Uri uri, int time) {
        MediaPlayer player = MediaPlayer.create(SiddhiAppService.getServiceInstance(), uri);
        player.setLooping(true);
        Timer r = new Timer();
        PeriodicRing periodicRing = new PeriodicRing(player, r);
        player.start();
        r.schedule(periodicRing, 0, time);
    }

    private class PeriodicRing extends TimerTask {
        boolean isFirst = true;
        MediaPlayer player;
        Timer timer;

        PeriodicRing(MediaPlayer player, Timer timer) {
            this.player = player;
            this.timer = timer;
        }

        @Override
        public void run() {
            if (isFirst) {
                isFirst = false;
                return;
            }
            player.stop();
            timer.cancel();
        }
    }
}
