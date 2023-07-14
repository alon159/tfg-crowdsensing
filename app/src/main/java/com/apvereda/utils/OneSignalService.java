package com.apvereda.utils;

import android.content.Context;
import android.util.Log;

import com.apvereda.db.Contact;
import com.couchbase.lite.Document;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.extension.siddhi.io.android.source.MessageHandler;
import org.wso2.siddhi.android.platform.SiddhiAppService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OneSignalService {
    public static Context context;

    public static void initialize(Context context){
        OneSignal.startInit(context)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                //.setNotificationReceivedHandler(new MessageHandler())
                .init();
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
                JSONObject notificationContent = new JSONObject(
                        "{" +
                                "'contents': {'en': '" + text + "'}," +
                                "'include_player_ids': [" + rec + "], " +
                                "'headings': {'en': '" + title + "'}, " +
                                "'data': " + data +
                                "}");
                Log.i("Message sent", notificationContent.toString());
                OneSignal.postNotification(notificationContent, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getUserID(){
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String userId = status.getSubscriptionStatus().getUserId();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();
        Log.i("OneSignalExample", "Subscription Status, is subscribed:" + isSubscribed);
        return isSubscribed ?  userId :  null;
    }
}
