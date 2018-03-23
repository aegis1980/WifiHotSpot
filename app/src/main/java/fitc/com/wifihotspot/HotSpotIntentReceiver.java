package fitc.com.wifihotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HotSpotIntentReceiver extends BroadcastReceiver {

    private final static String TAG = HotSpotIntentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received intent");
        HotSpotIntentService.startFromMagicActivity(context,intent);
    }
}
