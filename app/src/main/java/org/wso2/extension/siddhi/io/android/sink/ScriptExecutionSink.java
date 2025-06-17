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
import android.content.pm.PackageManager;
import android.health.connect.datatypes.ExerciseRoute;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Avatar;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.digitalavatars.ui.home.HomeFragment;
import com.apvereda.digitalavatars.ui.home.HomeViewModel;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatar;
import com.apvereda.utils.DigitalAvatarController;
import com.apvereda.utils.OneSignalService;
import com.couchbase.lite.MutableDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Sink to send android broadcasts.
 */
@Extension(
        name = "da-crowdpoll",
        namespace = "sink",
        description = "This will run scripts arriving to the stream using beanshell java interpreter.",
        parameters = {},
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
public class ScriptExecutionSink extends Sink {

    //private static final String APP_IDENTIFIER = "appid";
    //private static final String RECIPIENTS = "recipients";
    private String identifier;
    private String recipients;
    private Context context;
    private final ReentrantLock lock = new ReentrantLock();


    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        context = SiddhiAppService.getServiceInstance();
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
        Map<String, Object> event = (Map<String, Object>) o;
        Log.i("ScriptExecutionSink", "Poll received");
        filterReview((String) event.get("filter"));
        EntityType type = EntityType.fromText((String) event.get("type"));
        if (type == null) {
            Log.i("ScriptExecutionSink", "Invalid type of poll");
            return;
        }
        HomeViewModel vm = HomeViewModel.getInstance();
        switch (type) {
            case OFFER:
                if (Objects.requireNonNull(vm.getOfferBadgeVisibility().getValue()) != View.VISIBLE)
                    vm.setOfferBadgeVisibility(View.VISIBLE);
                break;
            case REQUEST:
                if (Objects.requireNonNull(vm.getRequestBadgeVisibility().getValue()) != View.VISIBLE)
                    vm.setRequestBadgeVisibility(View.VISIBLE);
                break;
        }
        DigitalAvatarController dac = new DigitalAvatarController();
        List<AbstractEntity> crowdpolls;
        lock.lock();
        try {
            crowdpolls = dac.getAll("DA-Poll" + event.get("pollId"), type);
            if (crowdpolls.isEmpty()) {
                createPollEntity(event);
            }
        } finally {
            lock.unlock();
        }
        //List crowdpolls = dac.getAll("DA-Poll"+event.get("pollId"));
        if (crowdpolls.isEmpty()) {
            //Log.i("DA-Crowdsensing", "Non existing Poll received");
            //createPollEntity(event);
            Log.i("DA-Crowdsensing", "Poll received with pollId " + event.get("pollId") + " and type " + event.get("type"));
            printCSV(context, "Poll received with pollId " + event.get("pollId"), new Date().toString());
            String role = (String) event.get("role");
            Executor exec = new Executor();
            exec.setCallback((String) event.get("callback"));
            exec.setScript((String) event.get("script"));
            exec.setPoll((String) event.get("pollId"));
            exec.setMasterTokenID((String) event.get("masterTokenID"));
            exec.setType(type);
            if (role.contains("Master")) {
                Log.i("DA-Crowdsensing", "My role is " + role);
                printCSV(context, "My role is " + role, new Date().toString());
                broadcastPoll(event, role);
                //final Handler handler = new Handler(Looper.getMainLooper());
                Log.i("DA-Crowdsensing", "Preparing execution in " + event.get("timeout"));
                printCSV(context, "Preparing execution in " + event.get("timeout"), new Date().toString());
                //handler.postDelayed(exec, Long.parseLong((String) event.get("timeout")));
                Executors.newSingleThreadScheduledExecutor().schedule(exec, Long.parseLong((String) event.get("timeout")), TimeUnit.MILLISECONDS);
            } else {
                Log.i("DA-Crowdsensing", "My role is " + role);
                printCSV(context, "My role is " + role, new Date().toString());
                Log.i("DA-Crowdsensing", "Preparing execution in " + event.get("timeout"));
                printCSV(context, "Preparing execution in " + event.get("timeout"), new Date().toString());
                Executors.newSingleThreadScheduledExecutor().schedule(exec, Long.parseLong((String) event.get("timeout")), TimeUnit.MILLISECONDS);
                //Thread thread = new Thread(exec);
                //thread.start();
            }
        }
    }

    private void createPollEntity(Map<String, Object> event) {
        String[] privacy = {"public,public"};
        Map<String, Value> values = new TreeMap<>();
        values.put("callback", new Value("callback", "String", privacy, new Date(), event.get("callback")));
        values.put("pollId", new Value("pollId", "String", privacy, new Date(), event.get("pollId")));
        values.put("survey", new Value("survey", "String", privacy, new Date(), event.get("survey")));
        values.put("results", new Value("results", "String", privacy, new Date(), "[]"));
        values.put("creator", new Value("creator", "Boolean", privacy, new Date(), false));
        Entity entity = new Entity(null, "DA-Poll" + event.get("pollId"), EntityType.fromText((String) event.get("type")), privacy, new Date(), values);
        Entity.create(entity);
        Log.i("DA-Crowdsensing", "Poll created: " + entity.getName() + " with type " + entity.getType().getText());
        printCSV(context, "Poll created: " + entity.getName(), new Date().toString());
    }

    private void broadcastPoll(Map<String, Object> event, String role) {
        Intent i = new Intent("broadcastPoll");
        int level = Integer.parseInt(role.split("-")[1]) - 1;
        String nextRole = level == 0 ? "Slave" : "Master-" + level;
        i.putExtra("role", nextRole);
        i.putExtra("pollId", (String) event.get("pollId"));
        i.putExtra("script", (String) event.get("script"));
        i.putExtra("survey", (String) event.get("survey"));
        i.putExtra("callback", Avatar.getAvatar().getOneSignalID());
        long nextTimeout = Long.parseLong((String) event.get("timeout")) / 2;
        i.putExtra("timeout", "" + nextTimeout);
        i.putExtra("masterTokenID", (String) event.get("masterTokenID"));
        SiddhiAppService.getServiceInstance().sendBroadcast(i);
        Log.i("DA-Crowdsensing", "Sending broadcast poll for " + nextRole + " with timeout " + nextTimeout);
        printCSV(context, "Sending broadcast poll for " + nextRole + " with timeout " + nextTimeout, new Date().toString());
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

    public void printCSV(Context context, String message, String time) {
        try {
            File fileDirectory = new File(context.getFilesDir(), "/Test");
            if (!fileDirectory.exists()) {
                fileDirectory.mkdir();
            }
            File root = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()), "Test");
            if (!root.exists()) {
                Log.i("Ficheros-DA", "La carpeta Test no existe " + root.getAbsolutePath());
                root.mkdirs();
            }
            File file = new File(root, "/Test.csv");
            FileWriter csvWriter = new FileWriter(file, true);
            csvWriter.append(Avatar.getAvatar().getOneSignalID() + " ; \"" + message + "\" ; " + time + "\n");
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterReview(String filters) {
        JSONObject filterJson;
        try {
            filterJson = new JSONObject(filters);
            if (filterJson.length() > 0) {
                Map<String, Object> additionalData = Avatar.getAvatar().getAdditionalData();
                for (Iterator<String> it = filterJson.keys(); it.hasNext(); ) {
                    String key = it.next();
                    switch (key) {
                        case "ubication":
                            JSONObject ubication = filterJson.getJSONObject(key);
                            int range = ubication.getInt("range");

                            JSONArray locationData = (JSONArray) ubication.get("location");
                            Location location = new Location("Poll");
                            location.setLatitude(locationData.getDouble(0));
                            location.setLongitude(locationData.getDouble(1));

                            Location myLocation;
                            try {
                                myLocation = obtainLocation();
                            } catch (ConnectionUnavailableException e) {
                                return;
                            }

                            if (location.distanceTo(myLocation) > range) {
                                return;
                            }
                            break;
                        case "age":
                            LocalDate today = LocalDate.now();
                            String birthDateText = (String) additionalData.get("birthDate");
                            LocalDate birthDate = LocalDate.parse(birthDateText);
                            Period period = Period.between(birthDate, today);
                            if (period.getYears() < filterJson.getInt(key)) {
                                return;
                            }
                            break;
                        case "genre":
                            if (additionalData.get("genre").equals(filterJson.getString(key))) {
                                return;
                            }
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private Location obtainLocation() throws ConnectionUnavailableException {
        LocationManager locationManager = (LocationManager) SiddhiAppService.getServiceInstance().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(SiddhiAppService.getServiceInstance(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SiddhiAppService.getServiceInstance(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            throw new ConnectionUnavailableException("Android Location permissions are not granted.");
        }
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}

class Executor implements Runnable {
    private String callback;
    private String scriptUrl;
    private String poll;
    private String masterTokenID;
    private EntityType type;

    @Override
    public void run() {
        try {
            //if answered????
            DigitalAvatarController dac = new DigitalAvatarController();
            Entity crowdpoll = (Entity) dac.getAll("DA-Poll" + poll, type).get(0);
            //scriptUrl = "https://raw.githubusercontent.com/alon159/tfg-crowdsensing/refs/heads/main/script.bsh"
            String script = getScript();
            final Interpreter i = new Interpreter();
            i.set("dac", new DigitalAvatarController());
            i.set("poll", poll);
            i.set("type", type);
            //i.set("myresult", ((Value)crowdpoll.get("myresult")).get()+"");
            Log.i("DA-Crowdsensing", "Script acquired " + script);
            i.eval(script);
            // RECOGER RESULTADO SCRIPT
            //String contactsResult = (String) ((Value) crowdpoll.get("results")).get();
            String result = (String) i.get("result");
            // SI SOY ESCLAVO MANDO RESPONSE AL MASTER
            //if (callback.contains("onesignalid: ")) {
            //callback = callback.replace("onesignalid: ", "");
            if (result != null) {
                Intent intent = new Intent("pollResponse");
                intent.putExtra("recipient", callback);
                intent.putExtra("type", type.getText());
                intent.putExtra("pollId", poll);
                intent.putExtra("result", result);
                intent.putExtra("masterTokenID", masterTokenID);
                SiddhiAppService.getServiceInstance().sendBroadcast(intent);
                Log.i("DA-Crowdsensing", "Slave sending result " + result + " to sender " + callback);
            }

//                    } else { // SI SOY EL MASTER, ENTONCES MANDO RESPUESTA DIRECTO AL SERVIDOR
//                        //CAMBIAR ESTO PARA QUE SE GUARDEN EN LA ENTITY CORRESPONDIENTE
//                        callback = callback.replace("server_url: ", "");
//                        Log.i("DA-Crowdsensing", "Master sending result to server " + callback);
//                        printCSV(context, "Master sending result to " + callback + ": " + result, new Date().toString());
//                        Log.i("DA-Crowdsensing", "Result: " + result);
//                        //Log.i("DA-Crowdsensing", "Contacts Results: " + contactsResult);
//                        Toast toast = Toast.makeText(context, "Sending results: " + result, Toast.LENGTH_LONG);
//                        toast.show();
//                        //postHttpRequest(callback, result);
            //}
            //}
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            Log.i("ScriptExecutionSink", "Poll not found");
        }
    }

    @NonNull
    private String getScript() throws IOException {
        String script = "";
        URL externalURL = new URL(scriptUrl);
        BufferedReader in = new BufferedReader(new InputStreamReader(externalURL.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            script += inputLine + '\n';
        }
        in.close();
        return script;
    }

//        private void postHttpRequest(String request, String pollResult) {
//            String result = "";
//            try {
//                String urlParameters = "pollResult=" + pollResult;
//                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
//                int postDataLength = postData.length;
//                URL url = new URL(request);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setDoOutput(true);
//                conn.setInstanceFollowRedirects(false);
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setRequestProperty("charset", "utf-8");
//                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
//                conn.setUseCaches(false);
//                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
//                    wr.write(postData);
//                    wr.close();
//                }
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                result = br.readLine();
//                br.close();
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setScript(String script) {
        scriptUrl = script;
    }

    public void setPoll(String poll) {
        this.poll = poll;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public void setMasterTokenID(String masterTokenID) {
        this.masterTokenID = masterTokenID;
    }
}
