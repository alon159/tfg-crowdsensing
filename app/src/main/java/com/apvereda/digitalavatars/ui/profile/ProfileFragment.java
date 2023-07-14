package com.apvereda.digitalavatars.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apvereda.db.Avatar;
import com.apvereda.db.Contact;
import com.apvereda.digitalavatars.R;
import com.apvereda.utils.DigitalAvatar;
import com.couchbase.lite.Document;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    View root;
    CollapsingToolbarLayout layout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        root = inflater.inflate(R.layout.fragment_profile, container, false);

        layout = (CollapsingToolbarLayout) root.findViewById(R.id.profile_toolbar_layout);
        final Toolbar toolbar = (Toolbar) root.findViewById(R.id.profile_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("Digital Avatars", "Intentando abrir el cajon");
                DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });
        /*NavController navController1 = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController1.getGraph()).build();
        NavigationUI.setupWithNavController(layout, toolbar, navController1, appBarConfiguration);*/

        //((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        Log.i("Digital Avatars", "Creando fragmento y procedo a cargarlos datos");
        //DigitalAvatar da = DigitalAvatar.getDA();
        //Document doc = da.getDoc("Avatar");
        updateAvatar();
        //Log.i("Digital Avatars", "Datos Cargados:" +a.getName() + ", " + a.getEmail());
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                DrawerLayout drawer = root.findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drawer, menu);

    }

    public void updateAvatar(){
        Avatar a = Avatar.getAvatar();
        TextView onesignal = root.findViewById(R.id.profileonesignal);
        onesignal.setText(a.getOneSignalID());
        TextView email = root.findViewById(R.id.profileemail);
        email.setText(a.getEmail());
        TextView mobile = root.findViewById(R.id.profilemobile);
        mobile.setText(a.getPhone());
        layout.setTitle(a.getName());
    }
}
