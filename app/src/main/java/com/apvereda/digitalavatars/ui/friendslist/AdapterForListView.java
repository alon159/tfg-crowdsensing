package com.apvereda.digitalavatars.ui.friendslist;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apvereda.db.Contact;
import com.apvereda.digitalavatars.R;


public class AdapterForListView extends BaseAdapter {
    Activity context;
    List<Contact> data;

    public AdapterForListView(Activity context, List<Contact> data) {
        super();
        this.context = context;
        this.data = data;
    }

    public void setData(List<Contact> data) {
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_friend, null);
        }

        //List<String> keys = da.getDoc("Relations").getKeys();
        TextView lblid = (TextView) convertView.findViewById(R.id.lblid);
        lblid.setText(data.get(position).getOneSignalID());
        TextView lblemail = (TextView) convertView.findViewById(R.id.lblemail);
        lblemail.setText(data.get(position).getEmail());
        //Log.i("Digital Avatar", "Pinto a:"+data.get(position).getEmail());
        ImageButton btndelete = convertView.findViewById(R.id.btndelete);
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact.deleteContactByEmail(data.get(position).getEmail());
                data.remove(position);
                notifyDataSetChanged();
                // DELETE OPINIONS
            }
        });
        /*List<RoutinePlace> places = db.getRoutinePlaces(data.get(position).getId());
        TextView lblAddress = (TextView) convertView.findViewById(R.id.lbladdress);
        lblAddress.setText(places.get(1).getPlace().getDescription());
        //lblAddress.setText(data.get(position).getLatitude()+"");

        TextView lblHour = (TextView) convertView.findViewById(R.id.lblhour);
        lblHour.setText(getDateTime(data.get(position).getStart()));
        //lblHour.setText(getDateTime(data.get(position).getDate()));
        */
        return (convertView);
    }

    private String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(date);
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

