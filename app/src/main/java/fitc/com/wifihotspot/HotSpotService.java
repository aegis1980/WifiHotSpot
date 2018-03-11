package fitc.com.wifihotspot;

import android.Manifest;
import android.app.IntentService;
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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

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
    // Action names...assigned in manifest.
    private  String ACTION_TURNON;
    private  String ACTION_TURNOFF;
    private  String DATAURI_TURNON;
    private  String DATAURI_TURNOFF;
    private Intent mStartIntent;
    private WifiManager.LocalOnlyHotspotCallback localOnlyHotspotCallback;


    private WifiManager.LocalOnlyHotspotReservation mReservation;

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
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);


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


    private void carryOn() {
        boolean turnOn = true;
        if (mStartIntent != null) {
            final String action = mStartIntent.getAction();
            final String data = mStartIntent.getDataString();
            if (ACTION_TURNON.equals(action) || (data!=null && data.contains(DATAURI_TURNON))) {
                turnOn = true;
            } else if (ACTION_TURNOFF.equals(action)|| (data!=null && data.contains(DATAURI_TURNOFF))) {
                turnOn = false;
            }

            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                turnOnHotspotOreo(turnOn);
            } else {
                turnOnHotspotPreOreo(turnOn);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTurnOnOld() {


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled())

        {
            wifiManager.setWifiEnabled(false);
        }

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = "MyAP";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try

        {
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
            }
            ;
            Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");

        } catch (
                Exception e)

        {
            Log.e(this.getClass().toString(), "", e);
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
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspotOreo(boolean turnOn){
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (turnOn) {
            try {
                manager.startLocalOnlyHotspot(MyApplication.getInstance().getHotspotCallback(), null);
            } catch (Exception e){
                //
            }
        } else if (MyApplication.getInstance().getHotspotReservation()!=null){
            MyApplication.getInstance().getHotspotReservation().close();
        }
    }




}

