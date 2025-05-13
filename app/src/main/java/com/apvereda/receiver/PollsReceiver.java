package com.apvereda.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.apvereda.db.Avatar;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatarController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PollsReceiver extends BroadcastReceiver {

    public PollsReceiver() {
        super();
    }

    @Override
        public void onReceive(Context context, Intent intent) {
            /**
             *  [
             *      {count:2, result:{track1:2,track2:2}},
             *      {count:4, result:{track1:4,track2:2}}
             *  ]
             */
            int q01 =0;
            if (intent.getAction().equals("receivePollResponse")) {
                String result = intent.getStringExtra("result");
                String count = intent.getStringExtra("count");
                result = "{count:"+count+", result:"+result+"}";
                DigitalAvatarController dac = new DigitalAvatarController();
                List polls = dac.getAll("DA-Poll"+intent.getStringExtra("pollId"), EntityType.OFFER);
                if(polls.size() != 0) {
                    Entity poll = (Entity) polls.get(0);
                    Value resultValue = (Value) poll.get("results");
                    String pollResult = (String) (resultValue).get();
                    if(!pollResult.equals("[]")) {
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
                }
            }
        }

    public void printCSV(Context context, String message, String time){
        try {
            File fileDirectory = new File(context.getFilesDir(), "/Test");
            if(!fileDirectory.exists()){
                fileDirectory.mkdir();
            }
            File root = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()), "Test");
            if (!root.exists()) {
                Log.i("Ficheros-DA", "La carpeta Test no existe "+root.getAbsolutePath());
                root.mkdirs();
            }
            File file = new File(root,"/Test.csv");
            FileWriter csvWriter =  new FileWriter(file, true);

            csvWriter.append(Avatar.getAvatar().getOneSignalID()+" ; \""+message+"\" ; "+time+"\n");
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
