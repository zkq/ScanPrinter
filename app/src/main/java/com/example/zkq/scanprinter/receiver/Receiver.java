package com.example.zkq.scanprinter.receiver;

/**
 * Created by zkq on 2016/9/22.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.serialport.api.SerialPort;
import android.util.Log;

import com.example.zkq.scanprinter.activity.MainActivity;
import com.smartdevicesdk.io.ScanGpio;

public class Receiver extends BroadcastReceiver {

    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, action);
        if (action.equals("com.zkc.keycode")) {
            int keyValue = intent.getIntExtra("keyvalue", 0);
            if (keyValue == 136 || keyValue == 135 || keyValue == 131) {
                long currentTime = System.currentTimeMillis();
                if(currentTime - MainActivity.pressTime < 500)
                {
                    //按键过于频繁
                    MainActivity.pressTime = currentTime;
                    return;
                }

                MainActivity.pressTime = currentTime;
                if (MainActivity.serialPort == null ||
                        !MainActivity.serialPort.isOpen() ||
                        !MainActivity.serialPort.isReading()) {
                    return;
                }
                MainActivity.serialPort.switchFunc(SerialPort.PORT.comScan);
                ScanGpio.openScan();
            }
        }
    }
}
