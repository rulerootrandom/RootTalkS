package com.rootjm.roottalks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ServiceStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        String action = intent.getAction();

        if (action.equals("android.intent.action.BOOT_COMPLETED")) { // 잘 동작된다..

            try {

                Intent rtService = new Intent( context, RootTalkService.class );

                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    context.startForegroundService(rtService);
                }
                else
                {
                    context.startService(rtService);
                }

                Log.d("Root Service", "부팅해서 RootTalkService 를 다시 시작합니다..");

            } catch( Exception e ) { Log.d("onReceive Exception: ", e.toString() ); }
        }
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
