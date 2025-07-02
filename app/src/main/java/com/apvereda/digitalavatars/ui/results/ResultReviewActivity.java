package com.apvereda.digitalavatars.ui.results;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.apvereda.digitalavatars.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ResultReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result_review);
        Bundle extras = getIntent().getExtras();
        String docResult = extras.getString("docResult");
        JSONObject jsonResult;
        String survey = extras.getString("survey");
        JSONArray jsonSurvey;
        try {
            jsonResult = new JSONObject(docResult);
            jsonSurvey = new JSONArray(survey);
            for (int i = 0; i < jsonSurvey.length(); i++) {
                JSONObject surveyResult = jsonResult.getJSONObject("q" + i);
                JSONObject surveyObject = jsonSurvey.getJSONObject(i);
                int numAnswers = surveyObject.length() - 1;
                String question = surveyObject.getString("questionText");
                String[] answers = new String[numAnswers];
                for (int j = 0; j < numAnswers; j++) {
                    answers[j] = surveyObject.getString("answer" + j);
                }
                generateSurveyResult(question, answers, surveyResult);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateSurveyResult(String question, String[] answers, JSONObject results) throws JSONException{
        LinearLayout layout = findViewById(R.id.surveyResultsLayout);
        LinearLayout surveyResult = new LinearLayout(this);
        surveyResult.setOrientation(LinearLayout.VERTICAL);
        TextView message = new TextView(this);
        message.setText("• "+question);
        message.setTextSize(25);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(15);
        surveyResult.addView(message, params);
        for (int i = 0; i < answers.length; i++) {
            LinearLayout answerLayout = new LinearLayout(this);
            answerLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView answerText = new TextView(this);
            answerText.setText(answers[i]+": ");
            answerText.setGravity(Gravity.END);
            answerText.setTextSize(20);
            answerLayout.addView(answerText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView answerResult = new TextView(this);
            answerResult.setText(results.optInt(i+"")+" persona/s");
            answerResult.setGravity(Gravity.START);
            answerResult.setTextSize(20);
            answerLayout.addView(answerResult, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            surveyResult.addView(answerLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        params.setMarginStart(0);
        params.setMargins(0,0,0,15);
        layout.addView(surveyResult, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    }
}