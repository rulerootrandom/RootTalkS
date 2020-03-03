package com.rootjm.roottalks;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    CheckMessageHandler cmHandler = new CheckMessageHandler();

    MalBallonAdapter malBallonAdapter;

    boolean bCmHandlerStart = false;

    String mUserName = " ";

    int iLogined = 0;

    Intent mServiceIntent = null;

    RootTalkService rootTalkService;

    boolean bServiceConnected = false;

    int iSelectedTab = 0;

    Tab1LoginFragment tab1LoginFragment;

    Tab2ChatFragment tab2ChatFragment;

    Tab3UserFragment tab3UserFragment;

    ServiceConnection conn = new ServiceConnection() {

        public void onServiceConnected( ComponentName name, IBinder service ) {

            RootTalkService.LocalBinder rts = (RootTalkService.LocalBinder) service;

            rootTalkService = rts.getService();

            rootTalkService.iBadgeCount = 0; // !!

            rootTalkService.updateIconBadgeCount(0); // !!

            Log.d("Root ", "Service Connected!!");

          //  /*
            if (rootTalkService.iServerConnStatus == 0)
            {
                rootTalkService.InitConnection( mUserName );
            }
          //  */

            bServiceConnected = true;
        }

        public void onServiceDisconnected( ComponentName name ) {

            Log.d( "Root ", "Service Disconnected!!");
            bServiceConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tab_main);

        SharedPreferences userlogin = getSharedPreferences("userlogin", MODE_PRIVATE );

        mUserName = userlogin.getString( "UserName", " " );
        iLogined = userlogin.getInt( "ILogined", 0 ); // int 로 하면 잘 저장된다..!!
        iSelectedTab = userlogin.getInt( "ISelected", 0 );

        StartAndBindRootTalkService();

        malBallonAdapter = new MalBallonAdapter();

        tab1LoginFragment = new Tab1LoginFragment();
        tab2ChatFragment = new Tab2ChatFragment();
        tab3UserFragment = new Tab3UserFragment();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("LogIn"));
        tabLayout.addTab(tabLayout.newTab().setText("TalkS"));
        tabLayout.addTab(tabLayout.newTab().setText("UserList"));

        switch(iSelectedTab)
        {
            case 0:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, tab1LoginFragment).commit();
                TabLayout.Tab tab = tabLayout.getTabAt(0 );
                tab.select();
            }
            break;

            case 1:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, tab2ChatFragment).commit();
                TabLayout.Tab tab = tabLayout.getTabAt(1 );
                tab.select();
            }
            break;

            case 2:
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, tab3UserFragment).commit();
                TabLayout.Tab tab = tabLayout.getTabAt(2);
                tab.select();
            }
            break;
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();

                Fragment selected = null;

                if(position == 0)
                {
                    selected = tab1LoginFragment;
                }
                else if(position == 1)
                {
                    selected = tab2ChatFragment;
                }
                else if(position == 2)
                {
                    selected = tab3UserFragment;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();

                // 선택 위치를 저장한다..
                SharedPreferences userloigin = getSharedPreferences("userlogin", MODE_PRIVATE );

                SharedPreferences.Editor editor = userloigin.edit();

                editor.putInt( "ISelected", position );

                editor.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // 키보드 위로 뷰를 자동으로 올려준다..
     //   getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        bCmHandlerStart = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

      //  Toast.makeText( this, "접속 실패: 서버가 꺼져있습니다..", Toast.LENGTH_LONG).show();

      //  bServiceConnected = false;

        /*
        if(bServiceConnected)
        {
           // unbindService(conn);
            //bServiceConnected = false;
        }
        */
    }

    // 현재 서비스가 실행 중인지 확인한다..
    public boolean IsServiceRunning()
    {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo serviceInfo: manager.getRunningServices(Integer.MAX_VALUE))
        {
            String sClassName = serviceInfo.service.getClassName();

            Log.d("Running Service:", sClassName );

            if(sClassName.equals("com.rootjm.roottalks.RootTalkService"))
            {
                return true;
            }
        }

        return false;
    }


    protected void onResume() {
        super.onResume();

        Toast.makeText( getApplicationContext(), "On Resume Called!!", Toast.LENGTH_LONG );

    }

    public void StartAndBindRootTalkService()
    {
        boolean bIsRun = IsServiceRunning();

        mServiceIntent = new Intent( MainActivity.this, RootTalkService.class );

        if( bIsRun == false)
        {
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                startForegroundService(mServiceIntent);
            }
            else
            {
                startService(mServiceIntent);
            }

            Log.d("RootTalkService", "RootTalk Service Started!!!");
        }
        else
        {
            Log.d( "RootTalkService", "RootTalk Service is already running!!");
        }

        bindService( mServiceIntent, conn, Context.BIND_AUTO_CREATE );
    }

    public void onStart() {
        super.onStart();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {
                    while(true) {

                        sleep( 100 );

                        Message msg = cmHandler.obtainMessage();
                        cmHandler.sendMessage(msg);

                    }
                } catch(Exception e) {

                }
            }
        });

        thread.start();

        Log.d(" Root Thread", "Thread is starting !!");
    }

    public void onStop() {
        super.onStop();
    }

    public void SendMessage( String sendMsg )
    {
      //  Toast.makeText( this, sendMsg, Toast.LENGTH_LONG ).show();

        if(!sendMsg.equals("") && bServiceConnected ) {

            if( (rootTalkService.iServerConnStatus == 1) )
                   // && (rootTalkService.iLogined == 1) )
            {
                rootTalkService.SetUserMessage(sendMsg);  // !! 요기까지 서버에서 자신이 보내는 메시지가 자신의 클라이언트에 보내지지 않도록 해야한다..

                long now = System.currentTimeMillis();

                Date datenow = new Date(now);

                SimpleDateFormat simpleDotFormat = new SimpleDateFormat("yyyy.MM.dd(HH:mm:ss)");
                String nowDate = simpleDotFormat.format(datenow);

                rootTalkService.InsertChatMsgDB(mUserName, sendMsg, nowDate);
            }

            tab2ChatFragment.editText.setText("");
        }
    }

     class CheckMessageHandler extends Handler {

        int SizeCount;
        int PastSizeCount = 0;

        public void handleMessage(Message msg) {

            if( bCmHandlerStart ) {

                if( bServiceConnected )
                {
                    SizeCount = rootTalkService.mChatMsgDB.GetTableSizeCount();

                    // 테이블의 사이즈가 변화할 때 마다 업데이트 한다..
                    if( SizeCount !=-1 && SizeCount>0 && SizeCount>PastSizeCount )
                    {
                        try {
                            tab2ChatFragment.chatMsgListView.setAdapter(malBallonAdapter);

                            rootTalkService.SelectChatMsgDB(malBallonAdapter);

                            tab2ChatFragment.chatMsgListView.setSelection(malBallonAdapter.getCount() - 1);

                            PastSizeCount = SizeCount;
                        }
                        catch(Exception e) {}
                    }
                }
            }
        }
    }
}
