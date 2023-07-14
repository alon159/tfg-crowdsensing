package org.wso2.extension.siddhi.io.android.source;

import android.content.Intent;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.siddhi.android.platform.SiddhiAppService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MessageHandler extends NotificationExtenderService {

    private String postHttpRequest(String request, String tokenID){
        String result ="";
        try {
            String urlParameters  = "idToken="+tokenID;
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;
            URL    url            = new URL( request );
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
                wr.close();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            result = br.readLine();
            br.close();
        } catch (ProtocolException e) {
        e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return result;
    }

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        JSONObject data = notification.payload.additionalData;
        String notificationID = notification.payload.notificationID;
        String title = notification.payload.title;
        String body = notification.payload.body;
        String smallIcon = notification.payload.smallIcon;
        String largeIcon = notification.payload.largeIcon;
        String bigPicture = notification.payload.bigPicture;
        String smallIconAccentColor = notification.payload.smallIconAccentColor;
        String sound = notification.payload.sound;
        String ledColor = notification.payload.ledColor;
        int lockScreenVisibility = notification.payload.lockScreenVisibility;
        String groupKey = notification.payload.groupKey;
        String groupMessage = notification.payload.groupMessage;
        String fromProjectNumber = notification.payload.fromProjectNumber;
        String rawPayload = notification.payload.rawPayload;
        String customKey;
        //Log.i("SiddhiMessage", "NotificationID received: " + notificationID);
        Log.i("DA-Crowdsensing", "Notification received from sender: " + title);


        try {
        // Verificar sender que es el title del mensaje
            if (data != null){
                //String tokenID = data.getString("tokenID");
                // NODE.JS SERVER TO GET FIREBASE USER EMAIL FROM TOKENID
                //String emailVerified = postHttpRequest("https://x.appspot.com/auth", data.getString("tokenID"));
                //Log.i("SiddhiMessage", "Verificando email: "+emailVerified+" : " + title);
                //if (title.equals(emailVerified)) {
                //if (title.equals(emailVerified)) {
                if (true) {
                    Intent i = new Intent(data.getString("appid"));
                    i.putExtra("sender", title);
                    i.putExtra("message", body);
                    Iterator<String> iterator = data.keys();
                    String key;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        if (data.get(key) instanceof Double) {
                            i.putExtra(key, data.getDouble(key));
                        }else if(data.get(key) instanceof String){
                            i.putExtra(key, data.getString(key));
                            //Log.i("DA-Crowdsensing", "message data key: " + key+" value: "+data.getString(key));
                        }
                    }
                    SiddhiAppService.getServiceInstance().sendBroadcast(i);
                }
            } else{
                Log.i("SiddhiMessage", "Error leyendo datos del mensaje");
            }
        } catch (JSONException e) {
                e.printStackTrace();
            }
        return true;
    }
}

