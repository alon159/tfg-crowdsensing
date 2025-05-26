package com.apvereda.digitalavatars.ui.pollsList;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatarController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SurveyActivity extends AppCompatActivity {

    String answer;
    Boolean subscriptionState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Bundle extras = getIntent().getExtras();
        String survey = extras.getString("survey");
        String pollId = extras.getString("pollId");
        MaterialButton subscriptionButton = findViewById(R.id.subscriptionButton);
        EntityType type = extras.getParcelable("type", EntityType.class);
        if (type == EntityType.REQUEST)
            subscriptionButton.setVisibility(View.VISIBLE);
        Log.i("DA-CrowdPoll", "Survey: " + survey);
        JSONObject jsonsurvey;
        try {
            jsonsurvey = new JSONObject(survey);
            TextView pollid = findViewById(R.id.pollid);
            pollid.setText(pollId);
            TextView message = findViewById(R.id.message);
            message.setText(jsonsurvey.getString("message"));
            MaterialButtonToggleGroup group = findViewById(R.id.decisionGroup);
            Button submitButton = findViewById(R.id.sendPollResultButton);
            group.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    MaterialButton button = findViewById(checkedId);
                    if (isChecked) {
                        if (checkedId == R.id.acceptButton) {
                            button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light));
                            answer = "accept";
                        } else if (checkedId == R.id.declineButton) {
                            button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
                            answer = "decline";
                        }
                        submitButton.setEnabled(true);
                    } else {
                        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                    }
                }
            });
            subscriptionButton.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    subscriptionState = !subscriptionState;
                    if (subscriptionState) {
                        subscriptionButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                        subscriptionButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));;
                        subscriptionButton.setIconTintResource(android.R.color.white);
                        subscriptionButton.setText("Suscrito");
                    }else{
                        subscriptionButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                        subscriptionButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                        subscriptionButton.setIconTintResource(android.R.color.holo_red_dark);
                        subscriptionButton.setText("Suscribete");
                    }
                }
            });
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialAlertDialogBuilder(view.getContext())
                            .setTitle("Confirmar decisión")
                            .setMessage("¿Estás seguro de que deseas enviar esta decisión?")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Aquí pones la lógica de envío
                                    String result = "{ result:" + answer + "}";
                                    DigitalAvatarController dac = new DigitalAvatarController();
                                    Entity poll = (Entity) dac.getAll(pollId, type).get(0);
                                    String[] privacy = {"public,public"};
                                    poll.set("myresult", new Value("myresult", "String", privacy, new Date(), result));
                                    //poll.set("subscription", new Value("subscription", "Boolean", privacy, new Date(), subscriptionState));
                                    Log.i("DA-Crowd", "Entity submmited: " + poll.getValues().keySet());
                                    Toast toast = Toast.makeText(view.getContext(), "Survey result recorded: " + result, Toast.LENGTH_LONG);
                                    toast.show();
                                    subscriptionButton.setVisibility(View.INVISIBLE);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();

                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        //JSONArray jsonsurvey;
//        JSONObject jsonsurvey;
//        try {
//            jsonsurvey = new JSONObject(survey);
//            ScrollView sv = new ScrollView(this);
//            LinearLayout ll = new LinearLayout(this);
//            ll.setOrientation(LinearLayout.VERTICAL);
//            sv.addView(ll);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 30, 0, 80);
//            ll.setPadding(100, 100, 100, 0);
//            TextView tittle = new TextView(this);
//            tittle.setText(pollId);
//            tittle.setTextSize(30);
//            ll.addView(tittle, layoutParams);
//            layoutParams.setMargins(0, 30, 0, 5);
//
//            TextView q = new TextView(this);
//            q.setText(jsonsurvey.getString("message"));
//            q.setTextSize(20);
//            ll.addView(q, layoutParams);
//
//            MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(this);
//            group.setOrientation(MaterialButtonToggleGroup.HORIZONTAL);
//            group.setSingleSelection(true);
//            group.setSelectionRequired(true);
//
//            MaterialButton btn1 = new MaterialButton(this);
//            btn1.setText(R.string.accept_offer);
//            group.addView(btn1);
//
//            MaterialButton btn2 = new MaterialButton(this);
//            btn2.setText(R.string.decline_offer);
//            group.addView(btn2);
//
////            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
////                @Override
////                public void onCheckedChanged(RadioGroup group, int checkedId) {
////                    RadioButton radioButton = (RadioButton) findViewById(checkedId);
////                    answers[Integer.parseInt(group.getTag() + "")] = (String) radioButton.getText();
////                }
////            });
//            group.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
//                @Override
//                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
//                    if (isChecked) {
//                        Button radioButton = (Button) findViewById(checkedId);
//                        answer = (String) radioButton.getText();
//                    }
//                }
//            });
//            ll.addView(group);
//
//        Button btn = new Button(this);
//        btn.setText("Submmit");
//        btn.setActivated(false);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String result = "{ result:"+answer+"}";
//                DigitalAvatarController dac = new DigitalAvatarController();
//                Entity poll = (Entity) dac.getAll(pollId, type).get(0);
//                String[] privacy = {"public,public"};
//                poll.set("myresult", new Value("myresult", "String", privacy, new Date(), result));
//                Log.i("DA-Crowd", "Entity submmited: " + poll.getValues().keySet());
//                Toast toast = Toast.makeText(view.getContext(), "Survey result recorded: " + result, Toast.LENGTH_LONG);
//                toast.show();
//            }
//        });
//        ll.addView(btn, layoutParams);
        //this.setContentView(sv);
//    } catch(JSONException e)
//
//    {
//        throw new RuntimeException(e);
//    }
    }
}