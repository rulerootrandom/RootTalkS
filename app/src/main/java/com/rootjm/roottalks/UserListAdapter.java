package com.rootjm.roottalks;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class UserListAdapter extends BaseAdapter {

    public class ListContents {

        String userID;

        ListContents( String _userID )
        {
            this.userID = _userID;
        }

    }

    private ArrayList<UserListAdapter.ListContents> m_List;

    public UserListAdapter()
    {
        m_List = new ArrayList<UserListAdapter.ListContents>();
    }

    public void clear()
    {
        m_List.clear();
    }

    public void add(String _userID )
    {
        m_List.add(new UserListAdapter.ListContents(_userID));
    }

    public void remove(int _position)
    {
        m_List.remove(_position);
    }

    public int getCount()
    {
        return m_List.size();
    }

    public Object getItem(int position)
    {
        return m_List.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        final int pos = position;
        final Context context = parent.getContext();

        TextView text = null;
        UserListAdapter.CustomHolder holder = null;
        LinearLayout layout = null;
   //     View viewRight = null;
   //     View viewLeft = null;

        if( convertView == null ) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.user_cell_layout, parent, false );

            layout = (LinearLayout) convertView.findViewById(R.id.usercell);
            text = (TextView) convertView.findViewById(R.id.userid);

         //   viewRight = (View) convertView.findViewById(R.id.imageViewRight);
         //   viewLeft = (View) convertView.findViewById(R.id.imageViewLeft);


            holder = new UserListAdapter.CustomHolder();
            holder.m_TextView = text;
            holder.layout = layout;

          //  holder.viewRight = viewRight;
          //  holder.viewLeft = viewLeft;

            convertView.setTag(holder);

        }
        else {
            holder = (UserListAdapter.CustomHolder) convertView.getTag();
            text = holder.m_TextView;
            layout = holder.layout;

         //   viewRight = holder.viewRight;
         //   viewLeft = holder.viewLeft;
        }

        text.setText(m_List.get(position).userID);

        /*
        if(m_List.get(position).type == 0 ) {
            text.setBackgroundResource(R.drawable.leftmal);
            layout.setGravity(Gravity.LEFT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            //  Toast.makeText(context, "Left Mal is called!!", Toast.LENGTH_LONG).show();
        }
        else if(m_List.get(position).type == 1 ) {
            text.setBackgroundResource(R.drawable.rightmal);
            layout.setGravity(Gravity.RIGHT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            // Toast.makeText(context, "Right Mal is called!!", Toast.LENGTH_LONG).show();
        }
        else if(m_List.get(position).type == 2 ) {
            text.setBackgroundResource(R.drawable.textbg);
            layout.setGravity(Gravity.CENTER);
            viewRight.setVisibility(View.VISIBLE);
            viewLeft.setVisibility(View.VISIBLE);

            //  Toast.makeText(context, "Center Mal is called!!", Toast.LENGTH_LONG).show();
        }
         */

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Toast.makeText(context, "List Clicked:"+m_List.get(pos), Toast.LENGTH_LONG).show();
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                // Toast.makeText( context, "List Long Clicked:" + m_List.get(pos), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        return convertView;
    }

    private class CustomHolder
    {
        TextView m_TextView;
        LinearLayout layout;
      //  View viewRight;
      //  View viewLeft;
    }
}
