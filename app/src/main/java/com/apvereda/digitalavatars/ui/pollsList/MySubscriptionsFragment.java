package com.apvereda.digitalavatars.ui.pollsList;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.amazonaws.services.sns.model.Subscription;
import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Entity;
import com.apvereda.digitalavatars.R;
import com.apvereda.utils.DigitalAvatar;
import com.apvereda.utils.DigitalAvatarController;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MySubscriptionsFragment extends AppCompatActivity {
    DigitalAvatar da;
    SubscriptionsAdapter adapter;
    ListView list;
    List<AbstractEntity> surveys;
    List<Subscription> subscriptions;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sns_subscriptions_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*CollapsingToolbarLayout layout = root.findViewById(R.id.friend_list_toolbar_layout);
        Toolbar toolbar = root.findViewById(R.id.friend_list_toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Digital Avatars", "Intentando abrir el cajon");
                DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });*/
        DigitalAvatarController dac = new DigitalAvatarController();
        List<AbstractEntity> aux = dac.getAllLike("DA-Poll");
        surveys = new ArrayList<>();
        for(int i=0; i<aux.size(); i++){
            Entity eaux = (Entity) aux.get(i);
            if(!eaux.getValues().containsKey("myresult")){
                surveys.add(eaux);
            }
        }
        adapter = new SubscriptionsAdapter(this,surveys);
        list = (ListView) findViewById(R.id.listTrips);
        list.setAdapter(adapter);
    }

    public void updateTrips(){
        DigitalAvatarController dac = new DigitalAvatarController();
        surveys = dac.getAllLike("DA-Poll");
        for(AbstractEntity e : surveys){
            Entity aux = (Entity) e;
            if(aux.getValues().containsKey("myresult")){
                surveys.remove(e);
            }
        }
        adapter.setData(surveys);
        list.setAdapter(adapter);
    }
}
