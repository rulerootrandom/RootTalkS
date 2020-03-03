package com.rootjm.roottalks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class Tab2ChatFragment extends Fragment {

    ListView chatMsgListView;

    EditText editText;

    Button sendBtn;

    MalBallonAdapter malBallonAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab2_chat, container, false);

        chatMsgListView = (ListView) rootView.findViewById(R.id.chatMsgList);

        malBallonAdapter = new MalBallonAdapter();

        editText = (EditText) rootView.findViewById(R.id.txtMsg);

        sendBtn = (Button) rootView.findViewById(R.id.sendBtn);


        FragmentActivity fa = getActivity();

        final MainActivity ma = (MainActivity) fa;

        try
        {
            chatMsgListView.setAdapter(malBallonAdapter);

            ChatMsgDBManager.getInstance().SelectData(malBallonAdapter);

            chatMsgListView.setSelection(malBallonAdapter.getCount() - 1);
        }
        catch(Exception e ) {}


        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = editText.getText().toString().trim();

               // ((MainActivity)getActivity()).SendMessage(msg);
                ma.SendMessage(msg);

            }
        });

        return rootView;
    }
}
