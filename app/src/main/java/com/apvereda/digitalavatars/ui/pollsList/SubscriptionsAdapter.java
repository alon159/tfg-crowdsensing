package com.apvereda.digitalavatars.ui.pollsList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.uDataTypes.EntityType;

import java.util.List;


public class SubscriptionsAdapter extends BaseAdapter {
    Activity context;
    List<AbstractEntity> data;
    EntityType type;

    public SubscriptionsAdapter(Activity context, List<AbstractEntity> data, EntityType type) {
        super();
        this.context = context;
        this.data = data;
        this.type = type;
    }

    public void setData(List<AbstractEntity> data) {
        this.data = data;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_sns_subscription, null);
        }
        Button btndelete = convertView.findViewById(R.id.btndelete);
        TextView lbltopic = (TextView) convertView.findViewById(R.id.lbltopicname);
        Entity poll = (Entity) data.get(position);
        lbltopic.setText(poll.getName());
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), SurveyActivity.class);
                i.putExtra("survey",((Value)poll.get("survey")).get()+"");
                i.putExtra("pollId",poll.getName());
                i.putExtra("type",type);
                view.getContext().startActivity(i);
            }
        });
        return convertView;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}

