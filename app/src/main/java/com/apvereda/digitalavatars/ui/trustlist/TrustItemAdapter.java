package com.apvereda.digitalavatars.ui.trustlist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apvereda.db.Contact;
import com.apvereda.db.TrustOpinion;
import com.apvereda.digitalavatars.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TrustItemAdapter extends BaseAdapter {
    Activity context;
    List<TrustOpinion> data;

    public TrustItemAdapter(Activity context, List<TrustOpinion> data) {
        super();
        this.context = context;
        this.data = data;

    }

    public void setData(List<TrustOpinion> data) {
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_trust, null);
        }

        //List<String> keys = da.getDoc("Relations").getKeys();
        TextView lbltruster = (TextView) convertView.findViewById(R.id.lbltruster);
        lbltruster.setText(data.get(position).getTruster());
        TextView lbltrustee = (TextView) convertView.findViewById(R.id.lbltrustee);
        lbltrustee.setText(Contact.getContact(data.get(position).getTrustee()).getEmail());
        //Log.i("DigitalAvatars", "El trustee es "+ data.get(position).getTrustee());
        //Log.i("DigitalAvatars", "y su email es "+ Contact.getContact(data.get(position).getTrustee()).getUID());
        //Log.i("DigitalAvatars", "contactos "+ Contact.getAllContacts().get(0).getEmail()+" - "+Contact.getAllContacts().get(0).getUID());
        TextView lbltrust = (TextView) convertView.findViewById(R.id.lbltrust);
        if(data.get(position).isReferral()){
            lbltrust.setText("Referral -> " + data.get(position).getTrust().projection());
        } else {
            lbltrust.setText("Trust -> " + data.get(position).getTrust().projection());
        }
        //Log.i("Digital Avatar", "Pinto a:"+data.get(position).getEmail());
        ImageButton btndelete = convertView.findViewById(R.id.btndelete);
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrustOpinion.deleteOpinion(data.get(position).getUid());
                data.remove(position);
                notifyDataSetChanged();
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

