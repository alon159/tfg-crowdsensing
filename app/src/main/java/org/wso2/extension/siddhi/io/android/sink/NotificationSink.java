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

import android.app.NotificationManager;
import android.content.Context;
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
 *Sink to create android notifications.
 */
@Extension(
        name = "android-notification",
        namespace = "sink",
        description = "This will publish events arriving to the stream through android " +
                "notifications",
        parameters = {
                @Parameter(
                        name = "multiple.notifications",
                        description = "If multipleNotifications is set as true new notification " +
                                "will be published for every new event arriving at the stream. " +
                                "Otherwise it will override the previously published " +
                                "notification if it is not cleared.",
                        optional = true,
                        type = {DataType.BOOL},
                        defaultValue = "false"
                ),
                @Parameter(
                        name = "title",
                        description = "Title of the notification message",
                        optional = true,
                        type = {DataType.STRING},
                        defaultValue = "Siddhi Platform"
                ),
                @Parameter(
                        name = "icon",
                        description = "Android id of notification icon image resource",
                        optional = true,
                        type = {DataType.INT},
                        defaultValue = "Default icon"
                )
        },
        examples = {
                @Example(
                        syntax = "@sink(type = 'android-notification',title = ‘Example’, " +
                                "icon = ‘1’, " +
                                "multiple.notifications = ‘true’,@map(type='keyvalue'," +
                                "@payload(message = 'Value is {{value}} taken from {{sensor}}')))\n"
                                + "define stream fooStream(sensor string, value float," +
                                " accuracy float)",
                        description = "This will publish events arrive to fooStream through " +
                                "android notifications which has title 'Example'. For each " +
                                "event it will send a new notification instead of updating " +
                                "the previous one. Notification will have format key:value"
                ),
                @Example(
                        syntax = "@sink(type = 'android-notification',title = ‘Example’, " +
                                "icon = ‘3’, " +
                                "multiple.notifications = ‘true’,@map(type='keyvalue'," +
                                "@payload('Value is {{value}} taken from {{sensor}}')))\n"
                                + "define stream fooStream(sensor string, value float," +
                                " accuracy float)",
                        description = "This will publish events arrive to fooStream through " +
                                "android notifications which has title 'Example'. For each " +
                                "event it will send a new notification instead of updating " +
                                "the previous one. Notification will contain the single message"
                )
        }
)

public class NotificationSink extends Sink {

    private NotificationManager notificationManager;
    private static final String SIDDHI_CHANNEL_ID = "org.wso2.extension.io.android.NOTIFICATION";
    private static final String SIDDHI_CHANNEL_NAME = "SIDDHI_EXTENSION_NOTIFICATION_CHANNEL";

    private static final String MULTIPLE_NOTIFICATION_STRING = "multiple.notifications";
    private static final String TITLE_STRING = "title";
    private static final String NOTIFICATION_ICON_STRING = "icon";

    private int notificationId = 101;
    private boolean multipleNotifications = false;
    private String title = "Siddhi Platform";
    private int icon = SiddhiAppService.getAppIcon();

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        multipleNotifications = Boolean.parseBoolean(optionHolder.validateAndGetStaticValue(
                MULTIPLE_NOTIFICATION_STRING, "false"));
        title = optionHolder.validateAndGetStaticValue(TITLE_STRING, "Siddhi Platform");
        icon = Integer.parseInt(optionHolder.validateAndGetStaticValue(NOTIFICATION_ICON_STRING,
                String.valueOf(SiddhiAppService.getAppIcon())));
        notificationManager = (NotificationManager) SiddhiAppService.getServiceInstance().
                getSystemService(Context.NOTIFICATION_SERVICE);
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
        String message = null;
        if (o instanceof Map) {
            Map<String, Object> mapInput = (Map<String, Object>) o;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry entry : mapInput.entrySet()) {
                if (entry.getKey() == null) {
                    sb.append(entry.getValue()).append("\n");
                } else {
                    sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                }
            }
            message = sb.toString();
        } else if (o instanceof String) {
            message = o.toString();
        }
        SiddhiAppService.getServiceInstance().createNotification
                (SIDDHI_CHANNEL_ID, SIDDHI_CHANNEL_NAME, title, message,
                        icon, this.notificationId, true);
        if (multipleNotifications) {
            this.notificationId += 1;
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void destroy() {
        this.notificationManager = null;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
    }

}

