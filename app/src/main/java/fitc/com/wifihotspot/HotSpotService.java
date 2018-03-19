package fitc.com.wifihotspot;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.ContentValues.TAG;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HotSpotService extends Service {

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
    private WifiManager.LocalOnlyHotspotReservation mHotspotReservation;

    @RequiresApi(api = Build.VERSION_CODES.O)
    MyOreoWifiManager mMyOreoWifiManager;


    /**
     * Flag for seeing if turning on in progress
     */
    private boolean mTurningOn;

    /**
     * Helper method to start this intent from {@link HotSpotIntentReceiver}
     * @param context
     * @param intent
     */
    public static void start(Context context,Intent intent) {
        Intent i = new Intent(context, HotSpotService.class);
        i.setAction(intent.getAction());
        i.setData(intent.getData());
        context.startService(i);
    }

    /**
     * Helper method to start this intent from {@link HotSpotIntentReceiver}
     * @param context
     * @param intent
     */
    public static void startFromMagicActivity(Context context,Intent intent) {
        Intent i = new Intent(context, HotSpotService.class);
        i.setData(intent.getData());
        context.startService(i);
    }



    public static void hotspotStatusChange(Context context, boolean isOn) {

    }

    //**********************************************************************************************

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;



    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {}
    }

    //**********************************************************************************************


    @Override
    public void onCreate() {
        ACTION_TURNON = getString(R.string.intent_action_turnon);
        ACTION_TURNOFF = getString(R.string.intent_action_turnoff);

        DATAURI_TURNON = getString(R.string.intent_data_host_turnon);
        DATAURI_TURNOFF = getString(R.string.intent_data_host_turnoff);


        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.i(TAG,"Received start intent");

        mStartIntent = intent;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        } else {
            carryOn();
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service done");
    }


    /**
     *
     */
    private void deferredStartForeground() {
        startForeground(FOREGROUND_ID,
                buildForegroundNotification());
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
     * <a>https://stackoverflow.com/questions/45984345/how-to-turn-on-off-wifi-hotspot-programmatically-in-android-8-0-oreo/45996578#45996578}</a>
     * This only open a local hotspot with no internet access. Fat load off good!
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Deprecated
    private void localHotspotOreo(boolean turnOn){
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (turnOn) {
            try {
                if (!mTurningOn) {
                    mTurningOn = true;
                    manager.startLocalOnlyHotspot(mLocalOnlyHotspotCallback, mServiceHandler);
                }
            } catch (Exception e){
                //
            }
        } else{
            if (mHotspotReservation!=null) {
                mHotspotReservation.close();

            } else {

            }
            stopForeground(true);
            stopSelf();
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

            mMyOreoWifiManager.startTethering(callback,mServiceHandler);
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

        Intent stopIntent = new Intent(this, HotSpotService.class);
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


    /**
     * This is all to do with Local Hot spots
     */
    @Deprecated
    WifiManager.LocalOnlyHotspotCallback mLocalOnlyHotspotCallback = new WifiManager.LocalOnlyHotspotCallback() {

        @Override
        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
            mTurningOn = false;
            deferredStartForeground();
            super.onStarted(reservation);
            Log.d(TAG, "Wifi Hotspot is on now");
            mHotspotReservation = reservation;
        }

        @Override
        public void onStopped() {
            mTurningOn = false;
            super.onStopped();

            stopForeground(true);
            stopSelf();
            Log.d(TAG, "onStopped: ");
        }

        @Override
        public void onFailed(int reason) {
            mTurningOn = false;
            super.onFailed(reason);

            stopForeground(true);
            stopSelf();
            Log.d(TAG, "onFailed: ");
        }
    };



}

