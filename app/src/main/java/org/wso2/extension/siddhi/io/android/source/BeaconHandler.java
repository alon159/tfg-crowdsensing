package org.wso2.extension.siddhi.io.android.source;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.wso2.siddhi.android.platform.SiddhiAppService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeaconHandler implements BeaconConsumer {
    private BeaconManager beaconManager;
    private List<Beacon> beaconsList = new ArrayList<>();
    Activity parentReceiverActivity;
    //private Map<String, MyBeacon> beaconIntervalsMap = getBeaconsIntervalsMap();
    //private Alarm myAlarm = Alarm.getInstance();


    private static BeaconHandler instance;

    public static BeaconHandler getInstance(){
        if(instance == null){
            instance = new BeaconHandler();
        }
        return instance;
    }

    private BeaconHandler(){ }

    public void setParentReceiverActivity(Activity activity){
        parentReceiverActivity = activity;
    }

    public void startBeaconScan(){
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(parentReceiverActivity);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.setBackgroundMode(false);
        beaconManager.setForegroundBetweenScanPeriod(100000);
        beaconManager.setForegroundScanPeriod(1500);
        beaconManager.bind(this);

    }

    @Override
    public Context getApplicationContext() {
        return parentReceiverActivity.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection connection) {
        parentReceiverActivity.unbindService(connection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection connection, int mode) {
        return parentReceiverActivity.bindService(intent, connection, mode);
    }


    public void beaconsReceived(final List<Beacon> beacons) {
        this.beaconsList = beacons;
        beaconManager.setForegroundBetweenScanPeriod(100000);
        for(Beacon beacon : beaconsList) {
            String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
            Intent i = new Intent("BEACON_HANDLER");
            i.putExtra("url", url);
            SiddhiAppService.getServiceInstance().sendBroadcast(i);
        }
        //parentReceiverActivity.updateList(beaconsList);

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {

            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconsReceived((List<Beacon>) beacons);
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("apveredaId1", null, null, null));
        } catch (RemoteException e) {
        }
    }

    /**
     * Initiates a new scan for beacons
     */
    public void rescan() {
        beaconManager.unbind(this);
        startBeaconScan();
    }

    public void unbind(){
        beaconManager.unbind(this);
    }
}
