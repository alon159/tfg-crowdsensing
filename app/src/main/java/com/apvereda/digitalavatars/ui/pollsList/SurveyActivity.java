package com.apvereda.digitalavatars.ui.pollsList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatarController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Bundle extras = getIntent().getExtras();
        String survey = extras.getString("survey");
        String pollId= extras.getString("pollId");
        EntityType type = extras.getParcelable("type", EntityType.class);
        Log.i("DA-CrowdPoll", "Survey: "+survey);
        JSONArray jsonsurvey;
        try {
            jsonsurvey = new JSONArray(survey);
            String[] answers = new String[jsonsurvey.length()];
            ScrollView sv = new ScrollView(this);
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            sv.addView(ll);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 30,0, 80);
            ll.setPadding(100, 100, 100, 0);
            TextView tittle = new TextView(this);
            tittle.setText(pollId);
            tittle.setTextSize(30);
            ll.addView(tittle,layoutParams);
            layoutParams.setMargins(0, 30,0, 5);

            for(int i = 0; i < jsonsurvey.length(); i++) {
                JSONObject question = (JSONObject) jsonsurvey.get(i);
                //radio list
                TextView q = new TextView(this);
                q.setText(question.getString("questionText"));
                q.setTextSize(20);
                ll.addView(q,layoutParams);

                RadioGroup group = new RadioGroup(this);
                group.setOrientation(RadioGroup.VERTICAL);

                RadioButton btn1 = new RadioButton(this);
                btn1.setText(question.getString("answer1"));
                group.addView(btn1);

                RadioButton btn2 = new RadioButton(this);
                btn2.setText(question.getString("answer2"));
                group.addView(btn2);

                group.setTag(i+"");
                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton radioButton = (RadioButton) findViewById(checkedId);
                        answers[Integer.parseInt(group.getTag()+"")] = (String)radioButton.getText();
                    }
                });
                ll.addView(group);

            }

            Button btn = new Button(this);
            btn.setText("Submmit");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String result = "{ ";
                    for(int i = 0; i<answers.length; i++){
                        result += "q"+i+" : "+answers[i]+", ";
                    }
                    result = result.substring(0,result.length()-2)+"}";
                    DigitalAvatarController dac = new DigitalAvatarController();
                    Entity poll = (Entity) dac.getAll(pollId, type).get(0);
                    String[] privacy =  {"public,public"};
                    poll.set("myresult",new Value("myresult","String",privacy, new Date(), result));
                    Log.i("DA-Crowd", "Entity submmited: "+ poll.getValues().keySet());
                    Toast toast =Toast.makeText(view.getContext(),"Survey result recorded: "+result, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            ll.addView(btn, layoutParams);
            this.setContentView(sv);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}