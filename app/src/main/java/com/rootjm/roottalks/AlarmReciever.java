package com.rootjm.roottalks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
       // throw new UnsupportedOperationException("Not yet implemented");
        try {

            Intent rtService = new Intent( context, RootTalkService.class );

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(rtService);
            }
            else
            {
                context.startService(rtService);
            }

            Log.d( "Root Talk", "RootService 가 다시 시작을 성공했습니다..");

        } catch( Exception e ) { Log.d("onReceive Exception: ", e.toString() ); }
    }
}
