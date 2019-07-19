package com.fitc.wifihotspot;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.fitc.wifihotspot.receiver.HotSpotIntentReceiver;

import java.lang.reflect.Method;

import static android.content.ContentValues.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class HotSpotIntentService extends IntentService {

    /**
     Id for running service in foreground
     */
    private static int FOREGROUND_ID=1338;
    private static final String CHANNEL_ID = "control_app";

    // Action names...assigned in manifest.
    private  String ACTION_TURNON;
    private  String ACTION_TURNOFF;
    private  String DATAURI_TURNON;
    private  String DATAURI_TURNOFF;
    private Intent mStartIntent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    MyOreoWifiManager mMyOreoWifiManager;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *

     */
    public HotSpotIntentService() {
        super("HotSpotIntentService");
    }

    /**
     * Helper method to start this intent from {@link HotSpotIntentReceiver}
     * @param context
     * @param intent
     */
    public static void start(Context context,Intent intent) {
        Intent i = new Intent(context, HotSpotIntentService.class);
        i.setAction(intent.getAction());
        i.setData(intent.getData());
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ACTION_TURNON = getString(R.string.intent_action_turnon);
        ACTION_TURNOFF = getString(R.string.intent_action_turnoff);

        DATAURI_TURNON = getString(R.string.intent_data_host_turnon);
        DATAURI_TURNOFF = getString(R.string.intent_data_host_turnoff);

        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.i(TAG,"Received start intent");

        mStartIntent = intent;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        } else {
            carryOn();
        }

    }


    private void carryOn() {
        boolean turnOn = true;
        if (mStartIntent != null) {
            final String action = mStartIntent.getAction();
            final String data = mStartIntent.getDataString();
            if (ACTION_TURNON.equals(action) || (data!=null && data.contains(DATAURI_TURNON))) {
                turnOn = true;
                Log.i(TAG,"Action/data to turn on hotspot");
            } else if (ACTION_TURNOFF.equals(action)|| (data!=null && data.contains(DATAURI_TURNOFF))) {
                turnOn = false;
                Log.i(TAG,"Action/data to turn off hotspot");
            }

            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                hotspotOreo(turnOn);
            } else {
                turnOnHotspotPreOreo(turnOn);
            }
        }
    }


    private boolean turnOnHotspotPreOreo(boolean turnOn) {
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            Method[] methods = wifiManager.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    try {
                        if (turnOn) {
                            wifiManager.setWifiEnabled(false); //Turning off wifi because tethering requires wifi to be off
                            method.invoke(wifiManager, null, true); //Activating tethering
                            return true;
                        } else {
                            method.invoke(wifiManager, null, false); //Deactivating tethering
                            wifiManager.setWifiEnabled(true); //Turning on wifi ...should probably be done from a saved setting
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }

            //Error setWifiApEnabled not found
            return false;
        }


    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void hotspotOreo(boolean turnOn){

        if (mMyOreoWifiManager ==null){
            mMyOreoWifiManager = new MyOreoWifiManager(this);
        }

        if (turnOn) {

            //this dont work
            MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback() {
                @Override
                public void onTetheringStarted() {
                    startForeground(FOREGROUND_ID,
                            buildForegroundNotification());
                }

                @Override
                public void onTetheringFailed() {

                }
            };

            mMyOreoWifiManager.startTethering(callback);
        } else{
            mMyOreoWifiManager.stopTethering();
            stopForeground(true);
            stopSelf();
        }

    }

    //****************************************************************************************


    /**
     * Build low priority notification for running this service as a foreground service.
     * @return
     */
    private Notification buildForegroundNotification() {
        registerNotifChnnl(this);

        Intent stopIntent = new Intent(this, HotSpotIntentService.class);
        stopIntent.setAction(getString(R.string.intent_action_turnoff));

        PendingIntent pendingIntent = PendingIntent.getService(this,0, stopIntent, 0);

        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID);
        b.setOngoing(true)
                .setContentTitle("WifiHotSpot is On")
                .addAction(new NotificationCompat.Action(
                        R.drawable.turn_off,
                        "TURN OFF HOTSPOT",
                        pendingIntent
                ))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.notif_hotspot_black_24dp);


        return(b.build());
    }


    private static void registerNotifChnnl(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager mngr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (mngr.getNotificationChannel(CHANNEL_ID) != null) {
                return;
            }
            //
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_chnnl),
                    NotificationManager.IMPORTANCE_LOW);
            // Configure the notification channel.
            channel.setDescription(context.getString(R.string.notification_chnnl_location_descr));
            channel.enableLights(false);
            channel.enableVibration(false);
            mngr.createNotificationChannel(channel);
        }
    }

}

