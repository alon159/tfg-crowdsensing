package com.apvereda.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.apvereda.receiver.PollsReceiver;

import org.wso2.siddhi.android.platform.SiddhiAppController;
import org.wso2.siddhi.android.platform.SiddhiAppService;

public class SiddhiService {
    public static final String MESSAGE_RECEIVER = "SiddhiMessage";
    private static PollsReceiver sbr;
    private static SiddhiAppController controller;
    private static ServiceConnection serviceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            controller = SiddhiAppController.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            controller = null;
        }
    };

    private SiddhiService() {
    }

    public static SiddhiAppController getServiceConnection(Context context) {
        if (controller != null) {
            return controller;
        }
        Intent intent = new Intent(context, SiddhiAppService.class);
        context.startService(intent);
        context.bindService(intent, serviceCon, context.BIND_AUTO_CREATE);
        return controller;
    }

    public static void registerReceivers(Context context){
        if (sbr == null) {
            sbr = new PollsReceiver();
        }
    }
}
