package com.apvereda.digitalavatars;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apvereda.db.Avatar;
import com.apvereda.db.Contact;
import com.apvereda.digitalavatars.ui.friendslist.MyFriendsFragment;
import com.apvereda.digitalavatars.ui.addfriend.AddFriendFragment;
import com.apvereda.digitalavatars.ui.home.HomeFragment;
import com.apvereda.digitalavatars.ui.profile.ProfileFragment;
import com.apvereda.utils.DigitalAvatar;
import com.apvereda.utils.OneSignalService;
import com.apvereda.utils.SiddhiService;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DrawerActivity extends AppCompatActivity {

    public static final int MESSAGE_NOT_ID = 101;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private DigitalAvatar da;
    NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    AddFriendFragment friendFragment = new AddFriendFragment();
    MyFriendsFragment friendlistFragment = new MyFriendsFragment();
    DrawerLayout drawer;
    Fragment[] fragments = new Fragment[]{homeFragment,profileFragment,friendFragment,friendlistFragment};
    String[] fragmentTAGS = new String[]{"home","profile","addfriend","friendlist"};
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            fragments[0] = getSupportFragmentManager().getFragment(savedInstanceState, "Home");
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                ft.show(fragments[0]);
                                ft.hide(fragments[1]);
                                ft.hide(fragments[2]);
                                ft.hide(fragments[3]);
                                toolbar.setVisibility(View.VISIBLE);
                                break;
                            case R.id.nav_profile:
                                profileFragment.updateAvatar();
                                ft.show(fragments[1]);
                                ft.hide(fragments[0]);
                                ft.hide(fragments[2]);
                                ft.hide(fragments[3]);
                                toolbar.setVisibility(View.GONE);
                                break;
                            case R.id.nav_add_friend:
                                ft.show(fragments[2]);
                                ft.hide(fragments[0]);
                                ft.hide(fragments[1]);
                                ft.hide(fragments[3]);
                                toolbar.setVisibility(View.GONE);
                                break;
                            case R.id.nav_friend_list:
                                friendlistFragment.updateFriends();
                                ft.show(fragments[3]);
                                ft.hide(fragments[0]);
                                ft.hide(fragments[1]);
                                ft.hide(fragments[2]);
                                toolbar.setVisibility(View.VISIBLE);
                                break;
                        }
                        //Log.i("Digital Avatars", "Paso por aqui al cambiar de fragment: bien hecho");
                        ft.commit();
                        menuItem.setChecked(true);
                        drawer.closeDrawers();
                        return true;
                    }
                });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragments[0], fragmentTAGS[0])
                .add(R.id.nav_host_fragment, fragments[1], fragmentTAGS[1])
                .add(R.id.nav_host_fragment, fragments[2], fragmentTAGS[2])
                .add(R.id.nav_host_fragment, fragments[3], fragmentTAGS[3])
                .hide(fragments[1])
                .hide(fragments[2])
                .hide(fragments[3])
                .commit();

        /*
        IMPORTANTE PRIMERO INICIALIZAR DA
         */
        checkAndRequestPermissions();
        SiddhiService.getServiceConnection(getApplicationContext());
        DigitalAvatar.init(getApplicationContext());
        da = DigitalAvatar.getDA();
        OneSignalService.initialize(getApplicationContext());
        createNotificationChannel();
        firebaseLogin();
    }

    private  boolean checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("DA-Permissions", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("DA-Permissions", "Some permissions are not granted ask again");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))  {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    Toast.makeText(DrawerActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                                            .show();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    @Override
    public void onBackPressed() {
        getSupportFragmentManager().beginTransaction()
                .show(fragments[0])
                .hide(fragments[1])
                .hide(fragments[2])
                .hide(fragments[3])
                .commit();
        toolbar.setVisibility(View.VISIBLE);
        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void firebaseLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            logUser();
        else{
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            // Create and launch sign-in intent
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        }

    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            logUser();
        } else {
            Log.i("Digital Avatar", "Log in fallido");
            if(response == null)
                Log.i("Digital Avatar", "User cancelled sing in flow pressing back button");
            else
                Log.i("Digital Avatar", response.getError().getMessage());

            }
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                //return true;
                break;
            case R.id.action_readapps:
                homeFragment.readFile();
                break;
            case R.id.action_removeapps:
                homeFragment.removeApps();
                break;
            case R.id.action_readfriends:
                readFriends();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createFriend(JSONObject friend) throws JSONException {
        Contact c = new Contact(friend.getString("email"), friend.getString("name"),
                "", "", friend.getString("onesignal"), "uid-"+friend.getString("email"));
        Contact.createContact(c);
    }

    private void readFriends() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrawerActivity.this);
        alertDialog.setTitle("Which user you are?");
        String[] items = {"User 1","User 2","User 3","User 4","User 5","User 6","User 7","User 8","User 9","User 10"};
        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String json="";
                    NetworkThread nt = new NetworkThread();
                    Thread t = new Thread(nt);
                    t.start();
                    t.join();
                    json=nt.getJson();
                    JSONArray friends = new JSONArray(json);
                    switch (which) {
                        case 0:
                            createFriend(friends.getJSONObject(1));
                            createFriend(friends.getJSONObject(2));
                            createFriend(friends.getJSONObject(3));
                            Toast.makeText(DrawerActivity.this, "Creating User 1 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 1:
                            createFriend(friends.getJSONObject(4));
                            createFriend(friends.getJSONObject(5));
                            Toast.makeText(DrawerActivity.this, "Creating User 2 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 2:
                            createFriend(friends.getJSONObject(6));
                            createFriend(friends.getJSONObject(7));
                            Toast.makeText(DrawerActivity.this, "Creating User 3 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 3:
                            createFriend(friends.getJSONObject(7));
                            Toast.makeText(DrawerActivity.this, "Creating User 4 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 4:
                            createFriend(friends.getJSONObject(8));
                            createFriend(friends.getJSONObject(9));
                            Toast.makeText(DrawerActivity.this, "Creating User 5 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 5:
                            createFriend(friends.getJSONObject(8));
                            createFriend(friends.getJSONObject(6));
                            Toast.makeText(DrawerActivity.this, "Creating User 6 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 6:
                            createFriend(friends.getJSONObject(9));
                            Toast.makeText(DrawerActivity.this, "Creating User 7 friendsS", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 7:
                            Toast.makeText(DrawerActivity.this, "Creating User 8 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 8:
                            createFriend(friends.getJSONObject(9));
                            Toast.makeText(DrawerActivity.this, "Creating User 9 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                        case 9:
                            Toast.makeText(DrawerActivity.this, "Creating User 10 friends", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            break;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        drawer.openDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "Home", fragments[0]);
    }

    private void logUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            Avatar.getAvatar().setIdToken(task.getResult().getToken());
                        } else {
                            Log.i("Digital Avatar", task.getException().getMessage());
                        }
                    }
                });
        Avatar avatar = Avatar.getAvatar();
        avatar.setEmail(user.getEmail());
        avatar.setName(user.getDisplayName());
        avatar.setOneSignalID(OneSignalService.getUserID());
        avatar.setUID(user.getUid());
        avatar.setPhone(user.getPhoneNumber());
        if (user.getPhotoUrl() != null)
            avatar.setPhoto(user.getPhotoUrl().toString());
        Log.i("Digital Avatar", "Datos personales almacenados en el avatar");
        loadUserView(user);
    }

    private void loadUserView(FirebaseUser user) {
        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.myname);
        name.setText(user.getDisplayName());
        TextView email = header.findViewById(R.id.myemail);
        email.setText(user.getEmail());
        ImageView image = header.findViewById(R.id.userimage);
        image.setImageURI(user.getPhotoUrl());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DigitalAvatarsChannel";
            String description = "DigitalAvatarsChannel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Digital-Avatars", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

class NetworkThread implements Runnable{
        String json;

        public String getJson(){
            return json;
        }

        @Override
        public void run() {
            try {
                json=getFriendsJSON();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String getFriendsJSON() throws IOException {
            String json ="";
            URL externalURL = new URL("https://xxx.000webhostapp.com/friends.json");
            BufferedReader in = new BufferedReader(new InputStreamReader(externalURL.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json += inputLine + '\n';
            }
            in.close();
            return json;
        }
    }
