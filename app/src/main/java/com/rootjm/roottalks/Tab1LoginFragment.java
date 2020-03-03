package com.rootjm.roottalks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class Tab1LoginFragment extends Fragment {

    public String mUserName;

    public int iLogined;

    private final Handler handler = new Handler();

    WebView webView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab1_login, container, false);

        webView = (WebView) rootView.findViewById(R.id.login);

        webView.setWebViewClient(new WebViewClient());

        WebSettings mWebSettings = webView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);

        mWebSettings.setBuiltInZoomControls(true);

        mWebSettings.setAppCacheEnabled(false);

        SharedPreferences userlogin = getContext().getSharedPreferences("userlogin", MODE_PRIVATE );

        mUserName = userlogin.getString( "UserName", " " );

        iLogined = userlogin.getInt( "ILogined", 0 ); // int 로 하면 잘 저장된다..!!

        if(iLogined == 1)
        {
            webView.loadUrl("http://rootjm.com/php/roottalk/logout.html");
        }
        else if(iLogined == 0)
        {
            webView.loadUrl("http://rootjm.com/php/roottalk/login.html");
        }

        webView.addJavascriptInterface(new JavascriptInterface(), "JSInterfaceName");

        return rootView;
    }

    final class JavascriptInterface {
        @android .webkit.JavascriptInterface

        public void callMethodName( final String str ) {

            handler.post( new Runnable() {
                @Override
                public void run() {

                    SharedPreferences userloigin = getContext().getSharedPreferences("userlogin", MODE_PRIVATE );

                    SharedPreferences.Editor editor = userloigin.edit();

                    mUserName = str;

                    iLogined = 1;

                    editor.putString("UserName", mUserName );

                    editor.putInt("ILogined", iLogined );

                    editor.commit();

                    FragmentActivity fa = getActivity();

                    final MainActivity ma = (MainActivity) fa;

                    try
                    {
                        ma.rootTalkService.CloseSocket();
                        ma.rootTalkService.InitConnection( mUserName );
                        ma.rootTalkService.iLogined = 1;
                        Toast.makeText( getContext(), "서버에 로그인 했습니다..", Toast.LENGTH_LONG ).show();
                    }
                    catch( Exception e ) {}

                }
            });
        }

        public void callMethodLogout()
        {

            handler.post( new Runnable() {
                @Override
                public void run() {

                    SharedPreferences userlogin = getContext().getSharedPreferences("userlogin", MODE_PRIVATE );

                    SharedPreferences.Editor editor = userlogin.edit();

                    iLogined = 0;

                    editor.putInt("ILogined", iLogined );

                    editor.commit();

                    FragmentActivity fa = getActivity();

                    final MainActivity ma = (MainActivity) fa;

                    try
                    {
                        ma.rootTalkService.CloseSocket();

                        ma.rootTalkService.iServerConnStatus = 0; // 요기까지!!!!!!

                        Toast.makeText( getContext(), "서버에서 로그 아웃 되었습니다..", Toast.LENGTH_LONG ).show();
                    }
                    catch(Exception e) {}
                }
            });
        }
    }
}
