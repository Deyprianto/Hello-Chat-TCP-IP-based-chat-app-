package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText name, ip, room, password;
    Button createBtn, joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name     = findViewById(R.id.name);
        ip       = findViewById(R.id.ip);
        room     = findViewById(R.id.room);
        password = findViewById(R.id.password);

        createBtn = findViewById(R.id.createBtn);
        joinBtn   = findViewById(R.id.joinBtn);

        createBtn.setOnClickListener(v -> openChat("create"));
        joinBtn.setOnClickListener(v   -> openChat("join"));
    }

    void openChat(String type) {
        // FIX: Validate required fields before proceeding
        String n = name.getText().toString().trim();
        String i = ip.getText().toString().trim();
        String r = room.getText().toString().trim();
        String p = password.getText().toString();

        if (n.isEmpty() || i.isEmpty() || r.isEmpty()) {
            Toast.makeText(this, "Name, IP and Room are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("name",     n);
        intent.putExtra("ip",       i);
        intent.putExtra("room",     r);
        intent.putExtra("password", p);
        intent.putExtra("type",     type);
        startActivity(intent);
    }
}