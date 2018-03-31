package com.fitc.wifihotspot;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends PermissionsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    void onPermissionsOkay() {

    }


    public void onClickTurnOnAction(View v){

    }

    public void onClickTurnOffAction(View v){

    }

    public void onClickTurnOnData(View v){
        MagicActivity.useMagicActivityToTurnOn(this);
    }

    public void onClickTurnOffData(View v){
        MagicActivity.useMagicActivityToTurnOff(this);
    }

}
