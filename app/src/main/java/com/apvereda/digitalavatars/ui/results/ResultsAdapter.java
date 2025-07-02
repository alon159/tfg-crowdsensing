package com.apvereda.digitalavatars.ui.results;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsAdapter extends BaseAdapter {

    Activity context;
    List<AbstractEntity> data;

    public ResultsAdapter(Activity context, List<AbstractEntity> data) {
        super();
        this.context = context;
        this.data = data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_result, null);
        }
        TextView lblPollId = convertView.findViewById(R.id.lblPollId);
        TextView lblState = convertView.findViewById(R.id.lblState);
        Entity poll = (Entity) data.get(position);
        String pollName = poll.getName();
        Value surveyValue = (Value) poll.getValue("survey");
        String survey = (String)surveyValue.get();
        lblPollId.setText(pollName);
        String pollId = pollName.substring("DA-Poll".length());
        MaterialButton btnReviewResult = convertView.findViewById(R.id.btnReviewResult);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pollResults")
                .whereEqualTo("pollId",pollId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().getDocuments().isEmpty()) {
                            DocumentSnapshot docResult = task.getResult().getDocuments().get(0);
                            Gson gson = new Gson();
                            String jsonResult = gson.toJson(docResult.getData().get("result"));
                            lblState.setText(R.string.label_results_available);
                            btnReviewResult.setEnabled(true);
                            btnReviewResult.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(v.getContext(), ResultReviewActivity.class);
                                    i.putExtra("docResult",jsonResult);
                                    i.putExtra("survey",survey);
                                    v.getContext().startActivity(i);
                                }
                            });
                        } else {
                            lblState.setText(R.string.label_results_no_available);
                            btnReviewResult.setEnabled(false);
                        }
                    }
                });
        return convertView;
    }
}
