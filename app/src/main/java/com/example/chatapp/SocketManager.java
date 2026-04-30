package com.example.chatapp;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager {

    String ip;
    int port;
    MessageAdapter adapter;

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    Handler handler = new Handler(Looper.getMainLooper());

    public interface ConnectionListener {
        void onConnected();
        void onError(String error);
    }

    private ConnectionListener listener;

    public SocketManager(String ip, int port, MessageAdapter adapter) {
        this.ip = ip;
        this.port = port;
        this.adapter = adapter;
    }

    public void setConnectionListener(ConnectionListener l) {
        this.listener = l;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (listener != null) handler.post(() -> listener.onConnected());

                String line;
                while ((line = in.readLine()) != null) {
                    final String finalLine = line;
                    handler.post(() -> {
                        try {
                            JSONObject obj = new JSONObject(finalLine);
                            String type = obj.optString("type");
                            String user = obj.optString("user", "");
                            String text = obj.optString("text", "");

                            if ("system".equals(type)) {
                                adapter.addRawMessage("• " + text, MessageAdapter.TYPE_SYSTEM);
                            } else if ("dm".equals(type)) {
                                // user field may be "You → bob" or sender name
                                adapter.addRawMessage(user + ": " + text, MessageAdapter.TYPE_DM);
                            } else if ("error".equals(type)) {
                                adapter.addRawMessage("⚠ " + text, MessageAdapter.TYPE_SYSTEM);
                            } else {
                                // FIX: pass user separately so adapter can detect own messages
                                adapter.addMessage(user, text, MessageAdapter.TYPE_RECEIVED);
                            }
                        } catch (Exception e) {
                            adapter.addRawMessage("Malformed message", MessageAdapter.TYPE_SYSTEM);
                        }
                    });
                }
            } catch (Exception e) {
                if (listener != null) handler.post(() -> listener.onError(e.getMessage()));
            }
        }).start();
    }

    public void send(String msg) {
        new Thread(() -> {
            if (out != null) out.println(msg); // println adds \n automatically
        }).start();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }
}