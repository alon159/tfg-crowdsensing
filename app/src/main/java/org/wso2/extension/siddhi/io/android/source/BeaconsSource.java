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
package org.wso2.extension.siddhi.io.android.source;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.gms.awareness.fence.FenceState;

import org.wso2.siddhi.android.platform.SiddhiAppService;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Source to receive android intent broadcasts.
 */
@Extension(
        name = "android-beacon",
        namespace = "source",
        description = "Broadcast source will consume internal android events from broadcast" +
                " intents from android os and apps. This source will output a key value map with" +
                "timestamp and what ever a key available in received intent. The keys " +
                "descriptions are available in" +
                " https://developer.android.com/reference/android/content/Intent.html" +
                "Event will be triggered only when a broadcast intent received to this source.",
        /*parameters = {
                @Parameter(
                        name = "appid",
                        description = "Broadcast source will catch intents which has this " +
                                "identifier name ",
                        type = {DataType.STRING}
                )
        },*/
        examples = {
                @Example(
                        syntax = "@source(type = 'android-broadcast',identifier = ‘SIDDHI_STREAM’" +
                                " ,@map(type='keyvalue'))\n" +
                                "define stream fooStream(state bool, timestamp long)",
                        description = "This will consume internal android events from broadcast" +
                                " intents which has action “SIDDHI_STREAM”.\n"
                )
        }
)
public class BeaconsSource extends Source {

    private SourceEventListener sourceEventListener;
    //private static final String APP_IDENTIFIER = "appid";
    private DataUpdateReceiver dataUpdateReceiver;
    private IntentFilter intentFilter;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] strings, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {

        this.sourceEventListener = sourceEventListener;
        this.dataUpdateReceiver = new DataUpdateReceiver();
        this.intentFilter = new IntentFilter();
        //this.intentFilter.addAction(optionHolder.validateAndGetStaticValue(
        this.intentFilter.addAction("BEACON_HANDLER");
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws
            ConnectionUnavailableException {
        SiddhiAppService.getServiceInstance().registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public void disconnect() {
        SiddhiAppService.getServiceInstance().unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    public void destroy() {
        dataUpdateReceiver = null;
    }

    @Override
    public void pause() {
        SiddhiAppService.getServiceInstance().unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    public void resume() {
        SiddhiAppService.getServiceInstance().registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            //Log.i("SiddhiSource", "Se ha recibido el evento con las claves"+ bundle.keySet());
            Map<String, Object> results = new HashMap<>();
            Long timestamp = System.currentTimeMillis() / 1000;
            results.put("timestamp", timestamp);
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    results.put(key, value);
                }
            }
            sourceEventListener.onEvent(results, null);
        }
    }
}
