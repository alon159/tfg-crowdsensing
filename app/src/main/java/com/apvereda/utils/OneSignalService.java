package com.apvereda.utils;

import android.content.Context;
import android.util.Log;
import com.apvereda.digitalavatars.BuildConfig;

import com.apvereda.db.Contact;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.List;

public class OneSignalService {
    public static Context context;
    private final static String ONESIGNAL_API_URL = "https://api.onesignal.com/notifications?c=push";
    private final static String ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID;
    private final static String ONESIGNAL_API_KEY = BuildConfig.ONESIGNAL_API_KEY;
    public static void initialize(Context context){
        // Enable verbose logging for debugging (remove in production)
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(context, ONESIGNAL_APP_ID);
    }

    public static void postMessage(String title, String text, String data, String recipients){
        //Log.i("OneSignalExample", "Message is:" + text);
        List<Contact> contacts;
        String rec="";
        try {
            if(recipients.equals("Relations")){
                contacts = Contact.getAllContacts();
                for(Contact c : contacts){
                    rec+= "'" + c.getOneSignalID() + "',";
                }
                //Implementar else if el nombre de algún grupo de privacidad
            } else {
                rec+= "'" + recipients + "',";
            }

            //DigitalAvatar da = DigitalAvatar.getDA();
            //Document doc = da.getDoc(recipients);
            //Iterator<String> it = doc.iterator();
            if(rec.length() > 0) {
                rec.substring(0, rec.length() - 1);
                JSONObject notificationContent = new JSONObject();
                notificationContent.put("app_id", ONESIGNAL_APP_ID);
                notificationContent.put("contents", new JSONObject().put("en", text));
                notificationContent.put("include_aliases", new JSONObject().put("onesignal_id", new JSONArray(rec)));
                notificationContent.put("target_channel", "push");
                notificationContent.put("headings", new JSONObject().put("en", title));
                notificationContent.put("data", new JSONObject(data));
                Log.i("Message sent", notificationContent.toString());
                postNotification(notificationContent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getUserID(){
        boolean isSubscribed = OneSignal.getNotifications().getPermission();
        String userId = OneSignal.getUser().getOnesignalId();
        Log.i("OneSignalExample", "Subscription Status, is subscribed:" + isSubscribed);
        return isSubscribed ?  userId :  null;
    }

    private static void postNotification(JSONObject jsonBody) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonBody.toString()
                );

                Request request = new Request.Builder()
                        .url(ONESIGNAL_API_URL)
                        .addHeader("Authorization", "Key " + ONESIGNAL_API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println("OneSignal Response: " + response.body().string());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
