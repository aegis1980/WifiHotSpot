package com.fitc.wifihotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class HotSpotIntentReceiver extends BroadcastReceiver {

    private final static String TAG = HotSpotIntentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String ACTION_TURNON = context.getString(R.string.intent_action_turnon);
        final String ACTION_TURNOFF = context.getString(R.string.intent_action_turnoff);
        Log.i(TAG,"Received intent");
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_TURNON.equals(action)) {
                MagicActivity.useMagicActivityToTurnOn(context);
            } else if (ACTION_TURNOFF.equals(action)) {
                MagicActivity.useMagicActivityToTurnOff(context);
            }
        }

    }
}
