package com.apvereda.digitalavatars.ui.trustlist;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.apvereda.db.Avatar;
import com.apvereda.db.TrustOpinion;
import com.apvereda.digitalavatars.R;
import com.apvereda.utils.DigitalAvatar;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class TrustListFragment extends AppCompatActivity {
    DigitalAvatar da;
    TrustItemAdapter adapter;
    ListView list;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_trust_list);
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
        da = DigitalAvatar.getDA();
        List<TrustOpinion> trips = TrustOpinion.getOpinionbyTruster(Avatar.getAvatar().getUID());
        for(TrustOpinion t : trips) t.setTruster("me");
        trips.addAll(TrustOpinion.getReferralOpinions());
        adapter = new TrustItemAdapter(this, trips);
        list = (ListView) findViewById(R.id.listTrust);
        list.setAdapter(adapter);

        /*NavController navController1 = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController1.getGraph()).build();
        NavigationUI.setupWithNavController(layout, toolbar, navController1, appBarConfiguration);*/

        //((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    }

    public void updateTrips(){
        List<TrustOpinion> trips = TrustOpinion.getOpinionbyTruster(Avatar.getAvatar().getUID());
        for(TrustOpinion t : trips) t.setTruster("me");
        trips.addAll(TrustOpinion.getReferralOpinions());
        Log.i("Digital Avatar", "Estos son los trust que pongo en la lista:"+trips);
        adapter.setData(trips);
        list.setAdapter(adapter);
    }
}

