package com.apvereda.digitalavatars.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apvereda.db.Avatar;
import com.apvereda.digitalavatars.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import org.wso2.siddhi.android.platform.SiddhiAppService;

public class CreateSurveyActivity extends AppCompatActivity {
    String surveyType;
    Boolean messageContentExists = false;
    String message;
    Long timeout;
    int scopeMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_survey);
        MaterialButton sendButton = findViewById(R.id.sendSurveyButton);
        MaterialButtonToggleGroup group = findViewById(R.id.tipoEncuestaToggle);
        group.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                MaterialButton button = findViewById(checkedId);
                if (isChecked) {
                    if (checkedId == R.id.btn_offer) {
                        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light));
                        surveyType = "offer";
                    } else if (checkedId == R.id.btn_request) {
                        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_light));
                        surveyType = "request";
                    }
                    //submitButton.setEnabled(true);
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                }
                if (messageContentExists)
                    sendButton.setEnabled(true);

            }
        });
        TextInputEditText messageTextInput = findViewById(R.id.textInputEncuesta);
        messageTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageContentExists = true;
                if (surveyType != null)
                    sendButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                message = s.toString().trim();
                sendButton.setEnabled(!message.isEmpty());
            }
        });
        MaterialButton btnToggle = findViewById(R.id.btnToggleAdvanced);
        LinearLayout advancedOptions = findViewById(R.id.advancedOptionsLayout);
        btnToggle.setOnClickListener(v -> {
            if (advancedOptions.getVisibility() == View.GONE) {
                advancedOptions.setVisibility(View.VISIBLE);
                btnToggle.setText("Ocultar Ajustes avanzados");
            } else {
                advancedOptions.setVisibility(View.GONE);
                btnToggle.setText("Mostrar Ajustes avanzados");
            }
        });

        TextInputEditText timeoutTextInput = findViewById(R.id.inputTimeout);
        timeout = Long.parseLong(timeoutTextInput.getText().toString());
        timeoutTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                    timeout = Long.parseLong(s.toString());
            }
        });
        TextInputEditText scopeTextInput = findViewById(R.id.inputScope);
        scopeMax = Integer.parseInt(scopeTextInput.getText().toString());
        scopeTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                    scopeMax = Integer.parseInt(s.toString());
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(view.getContext())
                        .setTitle("Confirmar decisión")
                        .setMessage("¿Estás seguro de que deseas enviar esta decisión?")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String scriptUrl = "https://raw.githubusercontent.com/alon159/tfg-crowdsensing/refs/heads/main/script.bsh";
                                Intent intent = new Intent("broadcastPoll");
                                String nextRole = (scopeMax - 1) == 0 ? "Slave" : "Master-" + (scopeMax - 1);
                                JSONObject survey = new JSONObject();
                                try {
                                    survey.put("message", message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //intent.putExtra("message", "Message body");
                                //intent.putExtra("recipient", "Relations");
                                //ADDITIONAL DATA FOR NOTIFICATION
                                intent.putExtra("type", surveyType);
                                intent.putExtra("role", nextRole);
                                intent.putExtra("pollId", ""+1);
                                intent.putExtra("timeout", "" + timeout);
                                //i.putExtra("pollId", (String) event.get("pollId"));
                                intent.putExtra("script", scriptUrl);
                                intent.putExtra("survey", survey.toString());
                                intent.putExtra("callback", Avatar.getAvatar().getOneSignalID());
                                SiddhiAppService.getServiceInstance().sendBroadcast(intent);
                                Log.i("DA-Crowdsensing", "Sending poll for " + nextRole + " with timeout " + timeout);
                                Toast toast = Toast.makeText(view.getContext(), "Poll sent", Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();

            }
        });
    }
}