package com.apvereda.digitalavatars.ui.friendslist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.apvereda.db.Contact;
import com.apvereda.digitalavatars.R;
import com.apvereda.digitalavatars.ui.trustlist.TrustListFragment;
import com.apvereda.utils.DigitalAvatar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class MyFriendsFragment extends Fragment {
    DigitalAvatar da;
    AdapterForListView adapter;
    ListView list;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friend_list, container, false);

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
        List<Contact> contacts = Contact.getAllContacts();
        adapter = new AdapterForListView(getActivity(), contacts);
        list = (ListView) root.findViewById(R.id.listFriends);
        list.setAdapter(adapter);

        FloatingActionButton fabtrust = root.findViewById(R.id.fabtrust);
        fabtrust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), TrustListFragment.class);
                startActivity(i);
            }
        });

        /*NavController navController1 = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController1.getGraph()).build();
        NavigationUI.setupWithNavController(layout, toolbar, navController1, appBarConfiguration);*/

        //((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        return root;
    }

    public void updateFriends(){
        List<Contact> contacts = Contact.getAllContacts();
        //Log.i("Digital Avatar", "Estos son los amigos que pongo en la lista:"+contacts);
        adapter.setData(contacts);
        list.setAdapter(adapter);
    }
}
