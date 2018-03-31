package com.fitc.wifihotspot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MagicActivity extends PermissionsActivity {

    public static void useMagicActivityToTurnOn(Context c){
        Uri uri = new Uri.Builder().scheme(c.getString(R.string.intent_data_scheme)).authority(c.getString(R.string.intent_data_host_turnon)).build();
        Toast.makeText(c,"Turn on. Uri: "+uri.toString(),Toast.LENGTH_LONG).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        c.startActivity(i);
    }

    public static void useMagicActivityToTurnOff(Context c){
        Uri uri = new Uri.Builder().scheme(c.getString(R.string.intent_data_scheme)).authority(c.getString(R.string.intent_data_host_turnoff)).build();
        Toast.makeText(c,"Turn off. Uri: "+uri.toString(),Toast.LENGTH_LONG).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        c.startActivity(i);
    }

    private static final String TAG = MagicActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100 ;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

    }

    @Override
    void onPermissionsOkay() {
        carryOnWithHotSpotting();
    }


    /**
     * The whole purpose of this activity - to start {@link HotSpotIntentService}
     * This may be called straright away in {@code onCreate} or after permissions granted.
     */
    private void carryOnWithHotSpotting() {
        Intent intent = getIntent();
        HotSpotIntentService.start(this, intent);
        finish();
    }




}
