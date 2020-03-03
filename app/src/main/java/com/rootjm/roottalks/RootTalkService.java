package com.rootjm.roottalks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class RootTalkService extends Service {

    String mUserName;

    public int iBadgeCount = 0;

    public ChatMsgDBManager mChatMsgDB;

    public int iServerConnStatus = 0;

    public int iLogined;

    MediaPlayer mMediaPlayer;  //Media Player 로 하면 사운드가 잘 재생되나 Sound Pool 로 하면 동작하지 않는다!!

    static {
        System.loadLibrary("native-lib");
    }

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {

        RootTalkService getService() {

            return RootTalkService.this;
        }
    }

    Thread serverConThread;

    boolean bServerConThreadStarted = false;

    ServerConHandler serverConHandler = new ServerConHandler();

    class ServerConHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            if( bServerConThreadStarted )
            {
                int i = ProcessCommunication(); //-----------------------------------------5

                if(i==1)
                {
                    InitConnection( mUserName );
                }
            }
        }
    }

    Thread  serverMessageThread;

    boolean bServerThreadStarted = false;

    ServerMessageHandler serverMessageHandler = new ServerMessageHandler();

    class ServerMessageHandler extends Handler {

        public void handleMessage(Message msg) {

            if( bServerThreadStarted )
            {
                String serverMsg = GetServerUserMessage().trim();  // 반드시 트림을 해주어야 한다 !!

                boolean bSpace = serverMsg.equals("");

                boolean bBraket = serverMsg.equals("[]");

                if((bSpace == false)&&(bBraket == false)) {

                    long now = System.currentTimeMillis();

                    Date datenow = new Date(now);

                    SimpleDateFormat simpleDotFormat = new SimpleDateFormat("yyyy.MM.dd(HH:mm:ss)");

                    String nowDate = simpleDotFormat.format(datenow);

                    InsertChatMsgDB( "Y!O!U", serverMsg, nowDate );

                    //    NotifyMessage( serverMsg );
                }
            }
        }
    }



    public RootTalkService() {

        mChatMsgDB = ChatMsgDBManager.getInstance();

        mChatMsgDB.SetContext( this );

        Log.d("RootTalkService", "서비스의 생성자 RootTalkService() 가 실행되었습니다..");
    }

    public void NotifyMessage( String serverMsg )
    {
        // 잘 된다.. Min SDK 를 15 로 낮추고 Target SDK 를 25 로 낮추면 Galaxy S2 와 Note 8  에서도 잘 동작한다..
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(this);

        Intent mainIntent = new Intent(this, MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );

        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, mainIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setTicker("알립니다");
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ja);
        builder.setContentTitle("서버로부터의 메시지");
        // builder.setContentText("오늘이 무슨 날인지 채크해 보세요");
        builder.setContentText(serverMsg);
        builder.setContentIntent(pendingIntent);
        builder.setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS );
        builder.setOngoing(true);

        notificationManager.notify(111, builder.getNotification());
    }


    public void InsertChatMsgDB( String userid, String chatmsg, String curtime )
    {
        mChatMsgDB.InsertData( userid, chatmsg, curtime );

        iBadgeCount++;

        updateIconBadgeCount( iBadgeCount );

        mMediaPlayer.start();
    }

    // MalBallonAdapter
    public void SelectChatMsgDB( MalBallonAdapter malBallonAdapter )
    {
        mChatMsgDB.SelectData( malBallonAdapter );   // !!! 오늘은 요기까지 !!!
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //  throw new UnsupportedOperationException("Not yet implemented");

        Log.d("RootTalkService", "서비스의 onBind 가 실행되었습니다..");

        return mBinder;
    }

    public void InitConnection( String sName )
    {
        mChatMsgDB.openDB();
        mChatMsgDB.createTable();

        int i = 0;

        i = ConnectToServer(); //-----------------------------------1

        if( i == 1 )
        {
            /*
            if( getApplicationContext() != null )
            {
               // Toast.makeText( getApplicationContext(), "접속 실패: 서버가 꺼져있습니다..", Toast.LENGTH_LONG).show();

              //  ((MainActivity) getApplicationContext()).finish();
            }
            */

            iServerConnStatus = 0;
        }

        SetUserName( sName ); //----------------------------------2

        i = SendUserIDToServer(); //-------------------------------------------3

        if( i == 1 )
        {
            /*
            if( getApplicationContext() != null )
            {
            //    Toast.makeText( getApplicationContext(), "전송 실패: 아이디를 전송하는데 실패했습니다..", Toast.LENGTH_LONG).show();

             //   ((MainActivity) getApplicationContext()).finish();
            }
             */

            iServerConnStatus = 0;
        }

        InitSocketSets(); //---------------------------------------------4

        if( i == 0 )
        {
            iServerConnStatus = 1;

            Toast.makeText( getApplicationContext(), "서버에 접속했습니다..", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        SharedPreferences userlogin = getSharedPreferences("userlogin", MODE_PRIVATE );

        iLogined = userlogin.getInt( "ILogined", 0 );

        mUserName = userlogin.getString( "UserName", " " );
        */

        mUserName = " ";
        iLogined = 0;


     //   if( iLogined == 1 )
      //  {
            if (iServerConnStatus == 0)
            {
                InitConnection( mUserName );   // 반드시 onCreate 에서 한번 서버와 연결해 주어야 한다..
            }
       // }

        mMediaPlayer = MediaPlayer.create(this, R.raw.chime_bell_positive_ring_02 );

        serverConThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    while(true) {

                        sleep( 500 );

                        Message msg = serverConHandler.obtainMessage();
                        serverConHandler.sendMessage(msg);

                    }

                } catch(Exception e) {

                }
            }
        });

        serverConThread.start();
        bServerConThreadStarted = true;

        serverMessageThread = new Thread(new Runnable() {

            public void run() {

                try {

                    while(true) {

                        sleep( 500 );

                        Message msg = serverMessageHandler.obtainMessage();
                        serverMessageHandler.sendMessage(msg);

                    }
                } catch(Exception e) {}
            }
        });

        serverMessageThread.start();
        bServerThreadStarted = true;

        Log.d("RootTalkService", "서비스 쓰레드가 시작 되었습니다..");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 앱이 종료되고 서비스가 다시 시작될 때 onStartCommand 가 다시 실행된다..

        Log.d("Root Talks ", "루트 서비스를 시작합니다..");

        return super.onStartCommand(intent, flags, startId);
    }

    protected void setAlarmTimer()
    {
        final Calendar c = Calendar.getInstance();

        c.setTimeInMillis((System.currentTimeMillis()));
        c.add(Calendar.SECOND, 1 );

        Intent intent = new Intent( this, AlarmReciever.class );

        PendingIntent sender = PendingIntent.getBroadcast( this, 0, intent, 0 );

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE );
        mAlarmManager.set( AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender );

        Log.d( "Root Talk", "Alarm 을 등록했습니다..");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 알람 서비스를 등록한다..
        setAlarmTimer();

        long now = System.currentTimeMillis();

        Date datenow = new Date(now);

        SimpleDateFormat simpleDotFormat = new SimpleDateFormat("yyyy.MM.dd(HH:mm:ss)");
        String nowDate = simpleDotFormat.format(datenow);

        mChatMsgDB.InsertData("Y!O!U", nowDate, nowDate );

        serverConThread.stop();
        bServerConThreadStarted = false;

        serverMessageThread.stop();
        bServerThreadStarted = false;

        Log.d("RootTalkService", "서비스의 onDestroy 가 실행되었습니다..");
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.d( "RootTalkService", "서비스의  Unbind 가 실행되었습니다..");
        return super.onUnbind(intent);
    }

    private String getLauncherClassName() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
      //  intent.setPackage(getPackageName());

        PackageManager pm = getApplicationContext().getPackageManager();

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for( ResolveInfo resolveInfo : resolveInfos ) {

            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;

            if(pkgName.equalsIgnoreCase(getPackageName())) {

                return resolveInfo.activityInfo.name;
            }
        }

        return null;
    }

    public void updateIconBadgeCount( int count ) {

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");

        intent.putExtra("badge_count", count );
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName());

        sendBroadcast(intent);
    }



    public native void SetUserMessage( String message);  // 서버로 보낼 메시지를 큐에 저장한다..

    public native String GetUserMessage();  //서버로 보낼 메시지 큐에서 메시지를 꺼내온다..

    public native void SetServerUserMessage( String message);  //서버에서 받은 메시지를 큐에 저장한다..

    public native String GetServerUserMessage();  // 서버에서 받은 메시지를 저장한 큐에서 메시지를 가져온다..

    public native int ConnectToServer(); //-----------------------------------1

    public native void SetUserName( String userName ); //----------------------------------2

    public native int SendUserIDToServer(); //-------------------------------------------3

    public native void InitSocketSets(); //---------------------------------------------4

    public native int ProcessCommunication(); //-----------------------------------------5

    public native void CloseSocket(); //---------------------------------------------6

    public native void SetUserList( String userId );

    public native String GetUserList();

    public native int QueryUserListFromServer();

    public native int GetUserListCount();
}
