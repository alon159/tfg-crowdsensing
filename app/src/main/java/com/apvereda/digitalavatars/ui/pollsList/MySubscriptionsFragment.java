package com.apvereda.digitalavatars.ui.pollsList;

import static com.apvereda.uDataTypes.EntityType.OFFER;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Entity;
import com.apvereda.digitalavatars.R;
import com.apvereda.digitalavatars.ui.home.HomeViewModel;
import com.apvereda.uDataTypes.EntityType;
import com.apvereda.utils.DigitalAvatar;
import com.apvereda.utils.DigitalAvatarController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MySubscriptionsFragment extends AppCompatActivity {
    DigitalAvatar da;
    SubscriptionsAdapter adapter;
    ListView list;
    List<AbstractEntity> surveys;
    EntityType type;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sns_subscriptions_list);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

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
//        type = getIntent().getExtras().getParcelable("type", EntityType.class);
        type = (EntityType) getIntent().getExtras().get("type");
        HomeViewModel vm = HomeViewModel.getInstance();
        switch (type){
            case OFFER:
                if (Objects.requireNonNull(vm.getOfferBadgeVisibility().getValue()) != View.INVISIBLE)
                    vm.setOfferBadgeVisibility(View.INVISIBLE);
                break;
            case REQUEST:
                if (Objects.requireNonNull(vm.getRequestBadgeVisibility().getValue()) != View.INVISIBLE)
                    vm.setRequestBadgeVisibility(View.INVISIBLE);
                break;
        }
        DigitalAvatarController dac = new DigitalAvatarController();
        List<AbstractEntity> aux = dac.getAllLike("DA-Poll", type);
        surveys = new ArrayList<>();
        for(int i=0; i<aux.size(); i++){
            Entity eaux = (Entity) aux.get(i);
            if(!eaux.getValues().containsKey("myresult")){
                surveys.add(eaux);
            }
        }
        TextView title = findViewById(R.id.subs_title);
        switch (type){
            case OFFER:
                title.setText(R.string.offer_assistance_title);
                break;
            case REQUEST:
                title.setText(R.string.request_assistance_title);
                break;
        }
        adapter = new SubscriptionsAdapter(this,surveys, type);
        list = findViewById(R.id.listTrips);
        list.setAdapter(adapter);
    }

    public void updateTrips(){
        DigitalAvatarController dac = new DigitalAvatarController();
        surveys = dac.getAllLike("DA-Poll", type);
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
