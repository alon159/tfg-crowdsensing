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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.wso2.siddhi.android.platform.SiddhiAppService;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

/**
 * Sink to send android broadcasts.
 */
@Extension(
        name = "android-broadcast",
        namespace = "sink",
        description = "This will publish events arriving to the stream through android broadcasts "
                + " intents which has attribute values as extras.",
        parameters = {
                @Parameter(
                        name = "identifier",
                        description = "Identifier is a mandatory parameter which represents the " +
                                "actions of the broadcast. This action is used by broadcast " +
                                "listeners to identify the intent. \n",
                        type = {DataType.STRING}
                )
        },
        examples = {
                @Example(
                        syntax = "@sink(type = 'android-broadcast' , identifier =" +
                                " 'SIDDHI_BROADCAST'," +
                                "@map(type='keyvalue',@payload(message = " +
                                "'Value is {{value}} taken from {{sensor}}')))\n" +
                                "define stream fooStream(sensor string, value float, " +
                                "accuracy float)",
                        description = "This will publish events arriving for fooStream as Intents" +
                                " which has key 'message' and value 'Value is...' string as extra "
                                +
                                "information in the intent. Intent Listeners should listen for " +
                                "SIDDHI_BROADCAST action to receive this intent."
                ),
                @Example(
                        syntax = "@sink(type = 'android-broadcast' , identifier = " +
                                "'SIDDHI_BROADCAST'," +
                                "@map(type='keyvalue'))\n" +
                                "define stream fooStream(sensor string, value float, " +
                                "accuracy float)",
                        description = "This will publish events arriving for fooStream as " +
                                "Intents" +
                                " which has keys 'sensor','value','accuracy' and respective " +
                                "values as extra " +
                                "information in the intent. Intent Listeners should listen for " +
                                "SIDDHI_BROADCAST action to receive this intent."
                )
        }
)
public class BroadcastIntentSink extends Sink {

    private static final String BROADCAST_FILTER_IDENTIFIER = "identifier";
    private String identifier;
    private Context context;

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        context = SiddhiAppService.getServiceInstance();
        identifier = optionHolder.validateAndGetStaticValue(BROADCAST_FILTER_IDENTIFIER);
        Log.i("DA", "Me he creado con identificador: " + identifier);
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
        Log.i("BroadcastIntentSink", "LLega evento a broadcast");
        Intent in = new Intent(identifier);
        if (o instanceof String) {
            in.putExtra("message", o.toString());
        } else if (o instanceof Map) {
            Map<String, Object> event = (Map<String, Object>) o;
            for (String key : event.keySet()) {
                Object value = event.get(key);
                if (value instanceof Integer) {
                    in.putExtra(key, (int) value);
                } else if (value instanceof Float) {
                    in.putExtra(key, (float) value);
                } else if (value instanceof Double) {
                    in.putExtra(key, (double) value);
                } else if (value instanceof Boolean) {
                    in.putExtra(key, (boolean) value);
                } else if (value instanceof Long) {
                    in.putExtra(key, (long) value);
                } else if (value instanceof String) {
                    in.putExtra(key, value.toString());
                } else {
                    in.putExtra(key, value.toString());
                }
            }
        }

        context.sendBroadcast(in);

    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}
