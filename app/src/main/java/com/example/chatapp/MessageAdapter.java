package com.example.chatapp;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MessageAdapter extends BaseAdapter {

    public static final int TYPE_SENT     = 0;
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_DM       = 2;
    public static final int TYPE_SYSTEM   = 3;

    static class MessageItem {
        String text;
        int type;
        MessageItem(String t, int tp) { text = t; type = tp; }
    }

    List<MessageItem> messages = new ArrayList<>();
    Context context;
    String ownUsername; // FIX: know who "self" is to pick correct bubble

    public MessageAdapter(Context c, String ownUsername) {
        context = c;
        this.ownUsername = ownUsername;
    }

    // FIX: Auto-detect bubble type from message content
    public void addMessage(String user, String text, int type) {
        // If the message user matches own username → show as sent bubble
        if (type == TYPE_RECEIVED && user.equals(ownUsername)) {
            type = TYPE_SENT;
        }
        String display = type == TYPE_SYSTEM ? text : user + ": " + text;
        messages.add(new MessageItem(display, type));
        notifyDataSetChanged();
    }

    public void addRawMessage(String msg, int type) {
        messages.add(new MessageItem(msg, type));
        notifyDataSetChanged();
    }

    @Override public int getCount()             { return messages.size(); }
    @Override public Object getItem(int i)      { return messages.get(i); }
    @Override public long getItemId(int i)      { return i; }
    @Override public int getViewTypeCount()     { return 4; }
    @Override public int getItemViewType(int i) { return messages.get(i).type; }

    @Override
    public View getView(int i, View v, ViewGroup parent) {
        MessageItem item = messages.get(i);

        if (v == null) {
            int layout = R.layout.message_row;
            if (item.type == TYPE_SENT)   layout = R.layout.message_row_sent;
            if (item.type == TYPE_SYSTEM) layout = R.layout.message_row_system;
            if (item.type == TYPE_DM)     layout = R.layout.message_row_dm;
            v = LayoutInflater.from(context).inflate(layout, parent, false);
        }

        TextView txt = v.findViewById(R.id.msg);
        txt.setText(item.text);
        return v;
    }
}