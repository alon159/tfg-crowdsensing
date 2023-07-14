package com.apvereda.digitalavatars.ui.home;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.apvereda.digitalavatars.DrawerActivity;
import com.apvereda.digitalavatars.R;
import com.apvereda.digitalavatars.ui.pollsList.MySubscriptionsFragment;
import com.apvereda.receiver.PollsReceiver;
import com.apvereda.utils.AmazonSNS;
import com.apvereda.utils.SiddhiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.wso2.extension.siddhi.io.android.source.BeaconHandler;
import org.wso2.siddhi.android.platform.SiddhiAppService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String appName="";
    View root;
    List<String> apps;
    List<String> appnames;
    PollsReceiver pollsReceiver;
    DataUpdateReceiver d;
    BeaconHandler beaconHandler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        setRetainInstance(true);
        if(savedInstanceState !=null) {
            //textView.setText(savedInstanceState.getString("text"));
            if(savedInstanceState.getInt("btn_play")==View.VISIBLE) {
                root.findViewById(R.id.fabplay).setVisibility(View.VISIBLE);
                root.findViewById(R.id.fabstop).setVisibility(View.GONE);
            } else{
                root.findViewById(R.id.fabplay).setVisibility(View.GONE);
                root.findViewById(R.id.fabstop).setVisibility(View.VISIBLE);
            }
        }
        /*new Thread(new Runnable(){
            @Override
            public void run() {
                AmazonSNS.getAmazonSNS("").listSubscriptions();
            }
        }).start();*/
        apps = new ArrayList<String>();
        appnames = new ArrayList<String>();
        //AmazonSNS.getAmazonSNS("").listSubscriptions();
        //SiddhiService.getServiceConnection(getActivity().getApplicationContext());
        Button subscriptions = root.findViewById(R.id.subscriptionsbtn);
        subscriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), MySubscriptionsFragment.class);
                startActivity(i);
            }
        });
        FloatingActionButton fabplay = root.findViewById(R.id.fabplay);
        fabplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startApp();
                    root.findViewById(R.id.fabplay).setVisibility(View.GONE);
                    root.findViewById(R.id.fabstop).setVisibility(View.VISIBLE);
                    Snackbar.make(view, "Siddhi app running", Snackbar.LENGTH_LONG).show();
                    //textView.setText("Siddhi app running");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        FloatingActionButton fabstop = root.findViewById(R.id.fabstop);
        fabstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopApp();
                    root.findViewById(R.id.fabplay).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.fabstop).setVisibility(View.GONE);
                    Snackbar.make(view, "Siddhi app stopped", Snackbar.LENGTH_LONG).show();
                    //textView.setText("Siddhi app stopped");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        root.findViewById(R.id.fabplay).setVisibility(View.VISIBLE);
        root.findViewById(R.id.fabstop).setVisibility(View.GONE);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState !=null) {
            //TextView textView = root.findViewById(R.id.text_home);
            //textView.setText(savedInstanceState.getString("text"));
            if(savedInstanceState.getInt("btn_play")==View.VISIBLE) {
                root.findViewById(R.id.fabplay).setVisibility(View.VISIBLE);
                root.findViewById(R.id.fabstop).setVisibility(View.GONE);
            } else{
                root.findViewById(R.id.fabplay).setVisibility(View.GONE);
                root.findViewById(R.id.fabstop).setVisibility(View.VISIBLE);
            }
        }

    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //TextView textView = root.findViewById(R.id.text_home);
        outState.putInt("btn_play", root.findViewById(R.id.fabplay).getVisibility());
        //outState.putString("text", textView.getText().toString());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    private void startApp() throws RemoteException {

        String app = "@app:name('PollsFlow')" +

                "@source(type='android-message', appid ='pollReceiver'," +
                "@map(type='keyvalue',fail.on.missing.attribute='false'," +
                "@attributes(role='role', pollId='pollId', script='script', callback='callback', timeout='timeout', survey='survey')))" +
                "define stream pollReceiver(role String, pollId String, script String, callback String, timeout String, survey String);" +

                "@source(type='android-message', appid ='pollResponseReceiver'," +
                "@map(type='keyvalue',fail.on.missing.attribute='false'," +
                "@attributes(pollId='pollId', count='count', result='message')))" +
                "define stream pollResponseReceiver(pollId String, count String, result String);" +

                "@source(type='android-broadcast', identifier='broadcastPoll'," +
                "@map(type='keyvalue',fail.on.missing.attribute='false'," +
                "@attributes(role='role', pollId='pollId', script='script', callback='callback', timeout='timeout', survey='survey')))" +
                "define stream pollBroadcast(role String, pollId String, script String, callback String, timeout String, survey String);" +

                "@source(type='android-broadcast', identifier='pollResponse'," +
                "@map(type='keyvalue',fail.on.missing.attribute='false'," +
                "@attributes(pollId='pollId', count='count', recipient='recipient', message='result')))" +
                "define stream pollResponse(pollId String, count String, recipient String, message String);" +


                "@sink(type='da-crowdpoll'," +
                "@map(type='keyvalue'))"+
                "define stream runPoll(role String, pollId String, script String, callback String, timeout String, survey String); " +

                "@sink(type='android-broadcast', identifier='receivePollResponse', " +
                "@map(type='keyvalue'))" +
                "define stream pollResponseReceived(pollId String, count String, result String); " +

                "@sink(type='android-message' , appid='pollReceiver', recipients='Relations'," +
                "@map(type='keyvalue'))"+
                "define stream sendPollBroadcast(role String, pollId String, script String, callback String, timeout String, survey String); " +

                "@sink(type='android-message' , appid='pollResponseReceiver', recipients='Relations'," +
                "@map(type='keyvalue'))"+
                "define stream sendPollResponse(pollId String, count String, recipient String, message String); " +

                "from pollResponse select * insert into sendPollResponse;"+
                "from pollReceiver select * insert into runPoll;"+
                "from pollResponseReceiver select * insert into pollResponseReceived;"+
                "from pollBroadcast select * insert into sendPollBroadcast;";


        apps.add(app);
        pollsReceiver = new PollsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("receivePollResponse");
        SiddhiAppService.getServiceInstance().registerReceiver(pollsReceiver, intentFilter);
        /*
        beaconHandler = BeaconHandler.getInstance();
        beaconHandler.setParentReceiverActivity(getActivity());
        beaconHandler.startBeaconScan();

        d = new DataUpdateReceiver();
        IntentFilter infi = new IntentFilter();
        infi.addAction("HumBroadcast");
        infi.addAction("Beacon");
        SiddhiAppService.getServiceInstance().registerReceiver(d, infi);
        //Log.i("DA", "Registro receiver");
        //this.getActivity().registerReceiver(d, infi);
        */
        for(String apps: apps) {
            appnames.add(SiddhiService.getServiceConnection(getActivity().getApplicationContext()).startSiddhiApp(apps));
        }
    }

    private void stopApp() throws RemoteException{
        for( String app : appnames) {
            SiddhiService.getServiceConnection(getContext()).stopSiddhiApp(app);
        }
        SiddhiAppService.getServiceInstance().unregisterReceiver(pollsReceiver);
        //beaconHandler.unbind();
        removeApps();
    }

    public void removeApps() {
        apps = new ArrayList<String>();
        appnames = new ArrayList<String>();
    }

    public void readFile(){
        apps = new ArrayList<String>();
        File fileDirectory = new File(getContext().getExternalFilesDir(null)+"/SiddhiApps");
        if(!fileDirectory.exists()){
            fileDirectory.mkdir();
        }
        File[] dirFiles = fileDirectory.listFiles();
        Snackbar.make(this.root, "Leyendo Siddhi Apps", Snackbar.LENGTH_LONG).show();
        String app = "";
        String line;
        for (File f : dirFiles) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while ((line = br.readLine()) != null){
                    app += line;
                }
                apps.add(app);
                System.out.println(app);
                app = "";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Digital-Avatars", "evento recibido");

            if (intent.getAction().equals("HumBroadcast")) {
                Log.i("Digital-Avatars-CEP", "Evento recibido: {"+intent.getStringExtra("sensor") + ", "+
                        intent.getStringExtra("value")+", "+intent.getStringExtra("time")+"}");
                Toast toast1 = Toast.makeText(context,
                        "Evento recibido: {"+intent.getStringExtra("sensor") + ", "+
                                intent.getStringExtra("value")+", "+intent.getStringExtra("time")+"}", Toast.LENGTH_LONG);
                toast1.show();
            }
            if (intent.getAction().equals("Beacon")) {
                Log.i("Digital-Avatars", "Beacon recibido con url: "+intent.getStringExtra("url"));
                Toast toast1 = Toast.makeText(context,
                        "Beacon recibido con url: "+intent.getStringExtra("url"), Toast.LENGTH_LONG);
                toast1.show();
            }
        }
    }
}
