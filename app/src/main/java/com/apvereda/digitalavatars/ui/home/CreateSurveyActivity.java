package com.apvereda.digitalavatars.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apvereda.db.Avatar;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.uDataTypes.EntityType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.extension.siddhi.io.android.source.LocationSource;
import org.wso2.siddhi.android.platform.SiddhiAppService;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class CreateSurveyActivity extends AppCompatActivity {
    String surveyType;
    Boolean messageContentExists = false;
    ArrayList<String> message = new ArrayList<>();
    ArrayList<String[]> answers = new ArrayList<>();
    int rangeFilterContent;
    Double[] ubicationFilterContent;
    int ageFilterContent;
    String[] genreFilterContent;
    Long timeout;
    int scopeMax;
    int surveysCount = 1;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

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
                    button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    if (checkedId == R.id.btn_offer) {
                        surveyType = "offer";
                    } else if (checkedId == R.id.btn_request) {
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
        MaterialCheckBox ubicationFilter = findViewById(R.id.ubicationFilter);
        LinearLayout ubicationSetting = findViewById(R.id.ubicationSetting);
        ubicationFilter.addOnCheckedStateChangedListener(new MaterialCheckBox.OnCheckedStateChangedListener() {

            @Override
            public void onCheckedStateChangedListener(@NonNull MaterialCheckBox checkBox, int state) {
                if (state == MaterialCheckBox.STATE_CHECKED) {
                    ubicationSetting.setVisibility(View.VISIBLE);
                } else {
                    ubicationSetting.setVisibility(View.GONE);
                }
            }
        });
        TextInputEditText rangoUbicacion = findViewById(R.id.rangoUbicacion);
        rangoUbicacion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    rangeFilterContent=Integer.parseInt(s.toString());
                }
            }
        });
        MaterialCheckBox ageFilter = findViewById(R.id.ageFilter);
        LinearLayout ageSetting = findViewById(R.id.ageSetting);
        ageFilter.addOnCheckedStateChangedListener(new MaterialCheckBox.OnCheckedStateChangedListener() {

            @Override
            public void onCheckedStateChangedListener(@NonNull MaterialCheckBox checkBox, int state) {
                if (state == MaterialCheckBox.STATE_CHECKED) {
                    ageSetting.setVisibility(View.VISIBLE);
                } else {
                    ageSetting.setVisibility(View.GONE);
                }
            }
        });
        TextInputEditText edadMinima = findViewById(R.id.edadMinima);
        edadMinima.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    ageFilterContent=Integer.parseInt(s.toString());
                }
            }
        });
        MaterialCheckBox genreFilter = findViewById(R.id.genreFilter);
        LinearLayout genreSetting = findViewById(R.id.genreSetting);
        genreFilter.addOnCheckedStateChangedListener(new MaterialCheckBox.OnCheckedStateChangedListener() {

            @Override
            public void onCheckedStateChangedListener(@NonNull MaterialCheckBox checkBox, int state) {
                if (state == MaterialCheckBox.STATE_CHECKED) {
                    genreSetting.setVisibility(View.VISIBLE);
                } else {
                    genreSetting.setVisibility(View.GONE);
                }
            }
        });
        TextInputEditText generoSelection = findViewById(R.id.generoSelection);
        generoSelection.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    genreFilterContent=s.toString().split(",\\s*");
                }
            }
        });
        LinearLayout layout = findViewById(R.id.survey0Layout);
        TextInputEditText messageTextInput = findViewById(R.id.textInputEncuesta);
        message.add(Integer.parseInt(layout.getTag().toString()), "");
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
                message.set(Integer.parseInt(layout.getTag().toString()), s.toString().trim());
                sendButton.setEnabled(!message.isEmpty());
            }
        });
        TextInputEditText inputNumAnswers = findViewById(R.id.inputNumAnswers);
        String[] answerAux = inputNumAnswers.getText().toString().split(",\\s*");
        Log.d("DA-Crowdsensing", "Answer: " + Arrays.toString(answerAux) + " at index: " + layout.getTag().toString());
        answers.add(Integer.parseInt(layout.getTag().toString()), answerAux);
        inputNumAnswers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    String[] answerAux = s.toString().split(",\\s*");
                    answers.set(Integer.parseInt(layout.getTag().toString()), answerAux);
                }
            }
        });
        MaterialButton addSurveyButton = findViewById(R.id.addSurveyButton);
        addSurveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSurvey(view);
            }
        });
        MaterialButton btnToggle = findViewById(R.id.btnToggleAdvanced);
        LinearLayout advancedOptions = findViewById(R.id.advancedOptionsLayout);
        btnToggle.setOnClickListener(v -> {
            if (advancedOptions.getVisibility() == View.GONE) {
                advancedOptions.setVisibility(View.VISIBLE);
                btnToggle.setText(R.string.advanced_settings_hide);
            } else {
                advancedOptions.setVisibility(View.GONE);
                btnToggle.setText(R.string.advanced_settings_show);
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
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.alert_message)
                        .setPositiveButton(R.string.alert_accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String scriptUrl = switch (surveyType) {
                                    case "offer" ->
                                            "https://raw.githubusercontent.com/alon159/tfg-crowdsensing/refs/heads/main/script.bsh";
                                    case "request" ->
                                            "https://raw.githubusercontent.com/alon159/tfg-crowdsensing/refs/heads/main/request_script.bsh";
                                    default -> "";
                                };
                                Intent intent = new Intent("broadcastPoll");
                                String nextRole = (scopeMax - 1) == 0 ? "Slave" : "Master-" + (scopeMax - 1);
                                StringBuilder survey = new StringBuilder("[");
                                Log.d("DA-Crowdsensing", "Message: " + message.toString());
                                for (int x = 0; x < message.size(); x++) {
                                    Log.d("DA-Crowdsensing", "Index: " + x);
                                    survey.append("{");
                                    survey.append("'questionText' : '").append(message.get(x)).append("',");
                                    String[] answersAux = answers.get(x);
                                    Log.d("DA-Crowdsensing", "Answer: " + Arrays.toString(answersAux) + " at index: " + x);
                                    for (int y = 0; y < answersAux.length; y++) {
                                        survey.append("'answer").append(y).append("' : '").append(answersAux[y]).append("',");
                                    }
                                    survey.delete(survey.length() - 1, survey.length());
                                    survey.append("},");
                                }
                                survey.delete(survey.length() - 1, survey.length());
                                survey.append("]");
                                String filters = convertFilters();
                                //intent.putExtra("message", "Message body");
                                //intent.putExtra("recipient", "Relations");
                                //ADDITIONAL DATA FOR NOTIFICATION
                                String pollId = generateId(4);
                                intent.putExtra("type", surveyType);
                                intent.putExtra("role", nextRole);
                                intent.putExtra("pollId", pollId);
                                intent.putExtra("timeout", "" + timeout);
                                //i.putExtra("pollId", (String) event.get("pollId"));
                                intent.putExtra("script", scriptUrl);
                                intent.putExtra("survey", survey.toString());
                                intent.putExtra("callback", Avatar.getAvatar().getOneSignalID());
                                intent.putExtra("filter", filters);
                                createPollEntity(Avatar.getAvatar().getOneSignalID(), pollId, survey.toString(), surveyType);
                                SiddhiAppService.getServiceInstance().sendBroadcast(intent);
                                Log.i("DA-Crowdsensing", "Sending poll for " + nextRole + " with timeout " + timeout);
                                Toast toast = Toast.makeText(view.getContext(), "Poll sent", Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.alert_cancel, null)
                        .show();

            }
        });
    }

    private void addSurvey(View view) {
        LinearLayout layout = findViewById(R.id.newSurveysLayout);
        //Layout para nueva encuesta
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setTag(surveysCount);
        surveysCount++;
        //Texto
        TextView contenidoLabel = new TextView(this);
        contenidoLabel.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        contenidoLabel.setText(R.string.survey_text_content);
        contenidoLabel.setTextSize(16);
        contenidoLabel.setPadding(0, 24, 0, 0);
        mainLayout.addView(contenidoLabel);
        //Texto encuesta
        TextInputLayout newSurveyLayout = new TextInputLayout(this);
        newSurveyLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(120)));
        TextInputEditText newSurvey = new TextInputEditText(newSurveyLayout.getContext());
        newSurvey.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        newSurvey.setHint(R.string.survey_text_content_hint);
        newSurvey.setRawInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        newSurvey.setGravity(Gravity.TOP);
        message.add(Integer.parseInt(mainLayout.getTag().toString()), "");
        newSurvey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                message.set(Integer.parseInt(mainLayout.getTag().toString()), s.toString().trim());
            }
        });
        newSurveyLayout.addView(newSurvey);
        mainLayout.addView(newSurveyLayout);
        //Respuestas encuesta
        LinearLayout settingsLayout = new LinearLayout(this);
        settingsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        settingsLayout.setOrientation(LinearLayout.HORIZONTAL);

// 3.1 TextView "Respuestas"
        TextView respuestasLabel = new TextView(this);
        respuestasLabel.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        respuestasLabel.setText(R.string.survey_text_answers);
        respuestasLabel.setTextSize(16);
        respuestasLabel.setPadding(0, 0, dpToPx(16), 0);
        settingsLayout.addView(respuestasLabel);

// 3.2 TextInputLayout + TextInputEditText
        TextInputLayout numAnswersLayout = new TextInputLayout(this);
        numAnswersLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextInputEditText inputNumAnswers = new TextInputEditText(numAnswersLayout.getContext());
        inputNumAnswers.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inputNumAnswers.setHint(R.string.survey_text_answers_hint);
        inputNumAnswers.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        inputNumAnswers.setText(getString(R.string.default_answers));
        String[] answerAux = inputNumAnswers.getText().toString().split(",\\s*");
        answers.add(Integer.parseInt(mainLayout.getTag().toString()), answerAux);
        inputNumAnswers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    String[] answerAux = s.toString().split(",\\s*");
                    answers.set(Integer.parseInt(mainLayout.getTag().toString()), answerAux);
                }
            }
        });
        numAnswersLayout.addView(inputNumAnswers);
        settingsLayout.addView(numAnswersLayout);
        mainLayout.addView(settingsLayout);
        //Codigo para añadir encuesta antes del botón
        int index = layout.indexOfChild(view);
        layout.addView(mainLayout, index);
    }

    private int dpToPx(int dp) {
        return Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics())
        );
    }

    private void createPollEntity(String callback, String pollId, String survey, String type) {
        String[] privacy = {"public,public"};
        Map<String, Value> values = new TreeMap<>();
        values.put("callback", new Value("callback", "String", privacy, new Date(), callback));
        values.put("pollId", new Value("pollId", "String", privacy, new Date(), pollId));
        values.put("survey", new Value("survey", "String", privacy, new Date(), survey));
        values.put("results", new Value("results", "String", privacy, new Date(), "[]"));
        values.put("creator", new Value("creator", "Boolean", privacy, new Date(), true));
        Entity entity = new Entity(null, "DA-Poll" + pollId, EntityType.fromText(type), privacy, new Date(), values);
        Entity.create(entity);
        Log.i("DA-Crowdsensing", "Poll created: " + entity.getName() + " with type " + entity.getType().getText());
        printCSV(this, "Poll created: " + entity.getName(), new Date().toString());
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

    public static String generateId(int length) {
        StringBuilder id = new StringBuilder(4);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }
        return id.toString();
    }

    private String convertFilters(){
        StringBuilder filter = new StringBuilder("{ ");
        if (rangeFilterContent != 0){
            double[] ubication;
            try {
                ubication = obtainLocation();
            } catch (ConnectionUnavailableException e) {
                throw new RuntimeException(e);
            }
            Log.d("ScriptExecution", "["+ubication[0]+", "+ubication[1]+"]");
            filter.append("'ubication' : ").append("{");
            filter.append("'range' : ").append(rangeFilterContent).append(",");
            filter.append("'location' : ").append("[").append(ubication[0]).append(",").append(ubication[1]).append("]");
            filter.append("},");
        }
        if (ageFilterContent!= 0)
            filter.append("'age' : ").append(ageFilterContent).append(",");
        if (genreFilterContent != null){
            filter.append("'genre' : [");
            for (String aux : genreFilterContent){
                filter.append("'").append(aux).append("', ");
            }
            filter.delete(filter.length() - 2, filter.length());
            filter.append("],");
        }

        filter.delete(filter.length() - 1, filter.length());
        filter.append("}");
        return filter.toString();
    }

    private double[] obtainLocation() throws ConnectionUnavailableException {
        LocationManager locationManager = (LocationManager) SiddhiAppService.getServiceInstance()
                .getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(SiddhiAppService.getServiceInstance(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(SiddhiAppService.getServiceInstance(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            throw new ConnectionUnavailableException("Android Location permissions are not granted.");
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            return new double[]{location.getLatitude(), location.getLongitude()};
        }
        return null;
    }

    public void updateUbication(JSONArray location) {
//        JSONObject aux;
//        try{
//            aux = new JSONObject(filters.getString("ubication"));
//            aux.put("location", location.toString());
//            filters.put("ubication", aux.toString());
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
    }
}