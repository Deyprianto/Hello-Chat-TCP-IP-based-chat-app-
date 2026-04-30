package com.example.chatapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

    SocketManager socketManager;
    MessageAdapter adapter;

    EditText messageBox, dmUser;
    TextView sendBtn;
    TextView roomTitle;

    String name, ip, room, password, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name     = getIntent().getStringExtra("name");
        ip       = getIntent().getStringExtra("ip");
        room     = getIntent().getStringExtra("room");
        password = getIntent().getStringExtra("password");
        type     = getIntent().getStringExtra("type");

        ListView listView = findViewById(R.id.listView);
        messageBox = findViewById(R.id.messageBox);
        dmUser     = findViewById(R.id.dmUser);
        sendBtn    = findViewById(R.id.sendBtn);
        roomTitle  = findViewById(R.id.roomTitle);

        roomTitle.setText("# " + room);

        adapter = new MessageAdapter(this, name); // pass own username for bubble type
        listView.setAdapter(adapter);

        socketManager = new SocketManager(ip, 5000, adapter);

        socketManager.setConnectionListener(new SocketManager.ConnectionListener() {
            @Override
            public void onConnected() {
                Toast.makeText(ChatActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                joinOrCreate();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        socketManager.connect();

        sendBtn.setOnClickListener(v -> {
            String msg  = messageBox.getText().toString().trim();
            String dmTo = dmUser.getText().toString().trim();

            if (msg.isEmpty()) return;

            try {
                JSONObject obj = new JSONObject();

                if (!dmTo.isEmpty()) {
                    obj.put("type", "dm");
                    obj.put("to", dmTo);
                    obj.put("user", name);
                    obj.put("text", msg);
                    dmUser.setText("");
                } else {
                    obj.put("type", "message");
                    obj.put("room", room);
                    obj.put("user", name);
                    obj.put("text", msg);
                }

                socketManager.send(obj.toString());
                messageBox.setText("");
                // NO local addMessage — server echoes back to everyone including sender

            } catch (Exception e) {
                Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinOrCreate() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "create".equals(type) ? "create_room" : "join");
            obj.put("room", room);
            obj.put("password", password);
            obj.put("user", name);
            socketManager.send(obj.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Could not join room", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) socketManager.disconnect();
    }
}