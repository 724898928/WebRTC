package com.lee.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lee.model.User;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    List<User> users;
    Context context;
    private ViewHolder viewHolder;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public MyAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return null!=users? users.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null!=users? users.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null != convertView){
            viewHolder = new ViewHolder();
            convertView =  LayoutInflater.from(context).inflate(R.layout.user_list_item,parent,false);
            viewHolder.textView = convertView.findViewById(R.id.tv);
            convertView.setTag(viewHolder);
        }else {
          viewHolder = (ViewHolder) convertView.getTag();
        }
        User user = users.get(position);
        if (null!= user){
            viewHolder.textView.setText(users.get(position).getName());
        }
        return null;
    }

    private final class ViewHolder{
        TextView textView;
    }
}
