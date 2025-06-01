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

import com.apvereda.utils.DigitalAvatar;
import com.apvereda.utils.OneSignalService;
import com.couchbase.lite.MutableDocument;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        name = "android-message",
        namespace = "sink",
        description = "This will publish events arriving to the stream through OneSignal messages.",
        parameters = {
                @Parameter(
                        name = "appid",
                        description = "Identifier is a mandatory parameter which represents the " +
                                "actions of the broadcast. This action is used by broadcast " +
                                "listeners to identify the intent. \n",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "recipients",
                        description = "Recipients of the message, it could be a list separated by ; " +
                                "or the path to a Relations collection in the Digital Avatar \n",
                        type = {DataType.STRING}
                )
        },
        examples = {
                @Example(
                        syntax = "@sink(type = 'android-message' , sender = 'SIDDHI_BROADCAST'," +
                                "@map(type='keyvalue',@payload(message = " +
                                "'Value is {{value}} taken from {{sensor}}')))\n" +
                                "define stream fooStream(sensor string, value float, " +
                                "accuracy float)",
                        description = "This will publish events arriving for fooStream as Message" +
                                " which is 'Value is...' string"
                ),
                @Example(
                        syntax = "@sink(type = 'android-message' , sender = 'SIDDHI_BROADCAST'," +
                                "@map(type='keyvalue'))\n" +
                                "define stream fooStream(sensor string, value float, " +
                                "accuracy float)",
                        description = "This will publish events arriving for fooStream as " +
                                "Message" +
                                " which has keys 'sensor','value','accuracy' and respective " +
                                "values as aditional data."
                )
        }
)
public class MessageSink extends Sink {

    private static final String APP_IDENTIFIER = "appid";
    private static final String RECIPIENTS = "recipients";
    private String identifier;
    private String recipients;
    private Context context;


    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        context = SiddhiAppService.getServiceInstance();
        identifier = optionHolder.validateAndGetStaticValue(APP_IDENTIFIER);
        recipients = optionHolder.validateAndGetStaticValue(RECIPIENTS);
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
        Log.i("Siddhi-Message", "Vamos a mandar mensaje");
        DigitalAvatar da = DigitalAvatar.getDA();
        MutableDocument doc = da.getDoc(da.getAvatars(), "Avatar");
        String title = doc.getString("Email");
        String tokenID = doc.getString("IDToken");
        String text ="{}", data="{}";
        Map<String, Object> event = (Map<String, Object>) o;
        if (event.containsKey("message")) {
            text = (String)event.get("message");
        }
        if (event.containsKey("recipient")) {
            recipients = (String)event.get("recipient");
        }
        StringBuilder sb = new StringBuilder("{");
        for (String key : event.keySet()) {
            Object value = event.get(key);
            sb.append("'"+key+"' : '"+value+"' , ");
        }
        sb.append("'tokenID' : '"+tokenID+"' , ");
        sb.append("'appid' : '"+identifier+"'");
        //sb.delete(sb.length()-3, sb.length()-1);
        sb.append("}");
        data = sb.toString();
        //Log.i("Siddhi-Message", "Se van a enviar los siguientes datos por mensaje: "+data);
        OneSignalService.postMessage(title,text,data,recipients);
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
