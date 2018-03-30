package com.fitc.wifihotspot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100 ;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 69;
    private static final int BOTH_PERMISSIONS_OK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingPermission();
        locationsPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int ok =0;
        // Check which request we're responding to
        if (requestCode == MY_PERMISSIONS_MANAGE_WRITE_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ok++;
            } else {
                settingPermission();
            }
        }

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ok++;
            } else {
                locationsPermission();
            }
        }

        boolean startedForPermissions = false;
        Intent startIntent = getIntent();
        if (startIntent!=null){
            String action = getString(R.string.needpermissions);
            if (action.equals(startIntent.getAction())){
                startedForPermissions = true;
            }
        }

        if (ok==BOTH_PERMISSIONS_OK && startedForPermissions){

        }
    }


    public void onClickTurnOnAction(View v){

    }

    public void onClickTurnOffAction(View v){

    }

    public void onClickTurnOnData(View v){
        Uri uri = new Uri.Builder().scheme(getString(R.string.intent_data_scheme)).authority(getString(R.string.intent_data_host_turnon)).build();
        Toast.makeText(this,"Turn on. Uri: "+uri.toString(),Toast.LENGTH_LONG).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        startActivity(i);
    }

    public void onClickTurnOffData(View v){
        Uri uri = new Uri.Builder().scheme(getString(R.string.intent_data_scheme)).authority(getString(R.string.intent_data_host_turnoff)).build();
        Toast.makeText(this,"Turn off. Uri: "+uri.toString(),Toast.LENGTH_LONG).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        startActivity(i);

    }

    public void settingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_PERMISSIONS_MANAGE_WRITE_SETTINGS);
            }
        }
    }


    private void locationsPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
