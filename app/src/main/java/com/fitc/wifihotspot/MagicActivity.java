package com.fitc.wifihotspot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;

public class MagicActivity extends Activity {

    private static final String TAG = MagicActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        settingsPermissionCheck();
        locationPermissionCheck();

    }

    /**
     *
     */
    private void locationPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(this,MainActivity.class);
            i.setAction(getString(R.string.needpermissions));
            i.setData(getIntent().getData());
            startActivity(i);
            finish();
        } else {
            // Permission has already been granted
            // Get the Intent that started this activity and extract the string
            carryOnWithHotSpotting();
        }
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
