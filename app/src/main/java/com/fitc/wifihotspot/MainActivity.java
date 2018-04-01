package com.fitc.wifihotspot;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends PermissionsActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView linkonTv = findViewById(R.id.linkon_tv);
        linkonTv.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView linkoffTv = findViewById(R.id.linkoff_tv);
        linkoffTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    void onPermissionsOkay() {

    }


    public void onClickTurnOnAction(View v){
        Intent intent = new Intent(getString(R.string.intent_action_turnon));
        sendBroadcast(intent);
    }

    public void onClickTurnOffAction(View v){
        Intent intent = new Intent(getString(R.string.intent_action_turnoff));
        sendBroadcast(intent);
    }

    public void onClickTurnOnData(View v){
        MagicActivity.useMagicActivityToTurnOn(this);
    }

    public void onClickTurnOffData(View v){
        MagicActivity.useMagicActivityToTurnOff(this);
    }

}
