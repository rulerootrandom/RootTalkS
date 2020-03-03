package com.rootjm.roottalks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import static java.lang.Thread.sleep;

public class Tab3UserFragment extends Fragment {

    ListView userListView;

    Button queryUserListBtn;

    UserListAdapter userListAdapter;

    FragmentActivity fa;

    MainActivity ma;

    public void QueryUserList()
    {
        if( ma.bServiceConnected )
        {
            try {

                ma.rootTalkService.QueryUserListFromServer();

                userListAdapter.clear();

                sleep(2000);

                int count = ma.rootTalkService.GetUserListCount();

                Log.d("UserListCount: ", "Count:"+count );

                for(int i=0; i<count; i++)
                {
                    String sUserId = ma.rootTalkService.GetUserList();

                    userListAdapter.add(sUserId);

                    Log.d("Adding UserID !!!", "UserCount:"+i);

                    userListView.setSelection(userListAdapter.getCount() - 1);
                }

                userListView.clearChoices();

                userListAdapter.notifyDataSetChanged();

            } catch (Exception e) {}
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance )
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab3_user, container, false);

        userListView = (ListView) rootView.findViewById(R.id.chatUserList);

        queryUserListBtn = (Button) rootView.findViewById(R.id.queryUserList);

        userListAdapter = new UserListAdapter();

        userListView.setAdapter(userListAdapter);

        fa = getActivity();

        ma = (MainActivity) fa;

        QueryUserList();

        queryUserListBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

             QueryUserList();

            }
        });

        return rootView;
    }
}
