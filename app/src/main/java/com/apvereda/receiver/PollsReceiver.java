package com.apvereda.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Avatar;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatarController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollsReceiver extends BroadcastReceiver {

    public PollsReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("receivePollResponse")) {
            Log.i("DA-Crowdsensing", "Poll response received");
            String pollId = intent.getStringExtra("pollId");
            String result = intent.getStringExtra("result");
            EntityType type = EntityType.fromText(intent.getStringExtra("type"));
            DigitalAvatarController dac = new DigitalAvatarController();
            List<AbstractEntity> polls = dac.getAll("DA-Poll" + pollId, type);
            if (!polls.isEmpty()) {
                Entity poll = (Entity) polls.get(0);
                Value resultValue = (Value) poll.get("results");
                String pollResult = (String) resultValue.get();
                if (!pollResult.equals("[]")) {
                    pollResult = pollResult.replace("]", ", " + result + "]");
                } else {
                    pollResult = pollResult.replace("]", result + "]");
                }
                resultValue.set(pollResult);
                poll.set("results", resultValue);
                Log.i("DA-Crowdsensing", "Poll response received: \"" + result + "\"");
                printCSV(context, "Poll response received", new Date().toString());
                Toast toast = Toast.makeText(context, "Poll response received: \"" + result + "\"", Toast.LENGTH_LONG);
                toast.show();
                Value creatorValue = (Value) poll.get("creator");
                if (creatorValue != null && (Boolean) creatorValue.get())
                    updateresults(poll, pollId, type, result);
            }
        }
    }

    private void updateresults(Entity poll, String pollId, EntityType type, String result) {
        Log.i("PollsReceiver", "Uploading poll results");
        try {
            //JSONObject polljson = new JSONObject(result);
            Gson gson = new Gson();
            Map<String, Object> jsonresult = gson.fromJson(result, Map.class);
            Map<String, Object> pollResult = new HashMap<>();
            pollResult.put("pollId", pollId);
            pollResult.put("type", type.getText());
            pollResult.put("result", jsonresult);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("pollResults")
                    .add(pollResult)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String[] privacy = {"public,public"};
                            poll.set("pollresultsid", new Value("pollresultsid", "String", privacy, new Date(), documentReference.getId()));
                            Log.d("PollReceiver", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.w("PollReceiver", "Error adding results to Firestore", e);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
           /* csvWriter.append("ID;Sent;Delivered\n");
            for(Message m : messages){
                csvWriter.append(m.getId()+";"+m.sent+";"+m.getDelivered()+"\n");
            }*/
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
