package com.rootjm.roottalks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChatMsgDBManager {

    private static ChatMsgDBManager instance = null;

    private SQLiteDatabase mSqLiteDatabase;

    Context mContext;


    private ChatMsgDBManager()
    {
    //    openDB();

    //    createTable();

     //   Log.d( "Root DB", "OpenDB 와 createTable 이 실행되었다..");
    }

    public static ChatMsgDBManager getInstance()
    {
        if(instance == null) {

            instance = new ChatMsgDBManager();

        }

        return instance;
    }

    public void SetContext( Context context )
    {
        mContext = context;
    }

    public void openDB()
    {
        DBHelper dbHelper = new DBHelper( mContext, "ChatMsg", null, 1 );

        try {

            mSqLiteDatabase = dbHelper.getWritableDatabase();

        } catch ( Exception e ) {}

    }

    public void createTable()
    {
        if( mSqLiteDatabase != null ){

            String sqLite = "create table if not exists ChatMsgTable " +
                    "( userid text, "+
                    "chatmsg text, curtime text )";

            mSqLiteDatabase.execSQL(sqLite);
        }
        else {
            //   Toast.makeText(getApplicationContext(),"먼저 데이터베이스를 오픈하세요", Toast.LENGTH_LONG).show();
        }
    }

  //  public void InsertData( String name, String date, String solarLunar, String kind, String before )
    public void InsertData( String userid, String chatmsg, String curtime )
    {
        if(mSqLiteDatabase != null) {

            String sqLite = "insert into ChatMsgTable( userid, chatmsg, curtime ) values(?,?,?)";

            Object[] params = { userid, chatmsg, curtime };

            mSqLiteDatabase.execSQL( sqLite, params );


           // Toast.makeText( mContext, chatmsg + "가 삽입되었습니다..", Toast.LENGTH_LONG ).show();
            Log.d("Root Msg Insert", chatmsg + "가 삽입되었습니다..");
        }
        else {
       //     Toast.makeText( mContext,"먼저 데이터베이스를 오픈하세요", Toast.LENGTH_LONG).show();
        }
    }

    public int GetTableSizeCount()
    {
        if( mSqLiteDatabase != null ) {

            //  String sqLite = "select name, date, solarLunar, kind, beforeDay from aLimTable";
            String sqLite = "select userid, chatmsg, curtime from ChatMsgTable";

            Cursor cursor = mSqLiteDatabase.rawQuery( sqLite, null );

            cursor.moveToFirst();

            int SizeCount = cursor.getCount();

            cursor.close();

            return SizeCount;
        }

        return -1;
    }

    public void SelectData( MalBallonAdapter malBallonAdapter )
    {
        if( mSqLiteDatabase != null ) {

            String sqLite = "select userid, chatmsg, curtime from ChatMsgTable";

            malBallonAdapter.clear();

            Cursor cursor = mSqLiteDatabase.rawQuery( sqLite, null );

            cursor.moveToFirst();

            for( int i=0; i<cursor.getCount(); i++ ) {

                String sUserID = cursor.getString(0);
                String sChatMsg = cursor.getString(1);
                String sCurTime = cursor.getString(2);

                if(sUserID.equals("Y!O!U"))  // 요기서 에러가 난다.. 왜 ? 내일 디버깅하자..
                {
                    malBallonAdapter.add(sChatMsg, 0);
                }
                else
                {
                    malBallonAdapter.add(sChatMsg, 1);
                }

                cursor.moveToNext();
            }

            cursor.close();

            malBallonAdapter.notifyDataSetChanged();  // 잘 된다..
        }
    }

    // Database Helper Class 작성..
    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            String sqLite = "create table if not exists ChatMsgTable " +
                    "( userid text, "+
                    "chatmsg text, curtime text )";

            sqLiteDatabase.execSQL( sqLite );

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            if(i1>1) {

                sqLiteDatabase.execSQL("drop table if exists ChatMsgTable");

                String sqLite = "create table if not exists ChatMsgTable " +
                        "( userid text, "+
                        "chatmsg text, curtime text )";

                sqLiteDatabase.execSQL( sqLite );
            }
        }
    }
}
