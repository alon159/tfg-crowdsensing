package com.apvereda.digitalavatars.ui.pollsList;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SurveyActivity extends AppCompatActivity {

    Boolean subscriptionState = false;
    String[] answers;
    int answersCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Bundle extras = getIntent().getExtras();
        String survey = extras.getString("survey");
        String pollId = extras.getString("pollId");
        EntityType type = (EntityType) extras.get("type");
        MaterialButton subscriptionButton = findViewById(R.id.subscriptionButton);
        if (type == EntityType.REQUEST)
            subscriptionButton.setVisibility(View.VISIBLE);
        subscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscriptionState = !subscriptionState;
                if (subscriptionState) {
                    subscriptionButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                    subscriptionButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));

                    subscriptionButton.setIconTintResource(android.R.color.white);
                    subscriptionButton.setText("Suscrito");
                } else {
                    subscriptionButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                    subscriptionButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                    subscriptionButton.setIconTintResource(android.R.color.holo_red_dark);
                    subscriptionButton.setText("Suscribete");
                }
            }
        });
        Log.i("DA-CrowdPoll", "Survey: " + survey);
        LinearLayout ll = findViewById(R.id.surveyLayout);
        TextView pollid = findViewById(R.id.pollid);
        pollid.setText(pollId);
        JSONArray jsonsurvey;
        try {
            jsonsurvey = new JSONArray(survey);
            answers = new String[jsonsurvey.length()];
            MaterialButton submitButton = new MaterialButton(this);
            for (int i = 0; i < jsonsurvey.length(); i++) {
                JSONObject question = jsonsurvey.getJSONObject(i);
                int numAnswers = question.length() - 1;
                //TEXTO ENCUESTA
                TextView message = new TextView(this);
                message.setText(question.getString("questionText"));
                message.setTextSize(14);
                ll.addView(message, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                //BOTONES DE RESPUESTA
                MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(this);
                group.setSingleSelection(true);
                group.setSelectionRequired(true);
                group.setOrientation(MaterialButtonToggleGroup.HORIZONTAL);
                //BOTONES
                for (int j = 0; j < numAnswers; j++) {
                    MaterialButton answer = new MaterialButton(
                            new ContextThemeWrapper(this, R.style.Widget_Material3_Button_OutlinedButton), null, 0);
                    answer.setText(question.getString("answer" + j));
                    answer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                    answer.setTag(j);
                    group.addView(answer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                }
                //LOGICA BOTONES
                group.setTag(i);
                group.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                        MaterialButton button = findViewById(checkedId);
                        if (isChecked) {
                            button.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                            button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            answers[Integer.parseInt(group.getTag().toString())] = button.getTag().toString();
                        } else {
                            button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blackTextColor));
                            button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                        }
                        answersCounter++;
                        if (answersCounter == jsonsurvey.length()) {
                            submitButton.setEnabled(true);
                        }
                    }
                });
                ll.addView(group, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            //BOTÓN ENVIAR
            submitButton.setText("Enviar");
            submitButton.setEnabled(false);
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
                                    String result = "{ ";
                                    for (int x = 0; x < answers.length; x++) {
                                        result += "q" + x + " : " + answers[x] + ", ";
                                    }
                                    result = result.substring(0, result.length() - 2) + "}";
                                    DigitalAvatarController dac = new DigitalAvatarController();
                                    Entity poll = (Entity) dac.getAll(pollId, type).get(0);
                                    String[] privacy = {"public,public"};
                                    poll.set("myresult", new Value("myresult", "String", privacy, new Date(), result));
                                    //poll.set("subscription", new Value("subscription", "Boolean", privacy, new Date(), subscriptionState));
                                    Log.i("DA-Crowd", "Entity submmited with this answers: " + result);
                                    Log.i("DA-Crowd", "Entity submmited: " + poll.getValues().keySet());
                                    Toast toast = Toast.makeText(view.getContext(), "Survey result recorded: " + result, Toast.LENGTH_LONG);
                                    toast.show();
                                    subscriptionButton.setVisibility(View.GONE);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();

                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            ll.addView(submitButton, params);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}