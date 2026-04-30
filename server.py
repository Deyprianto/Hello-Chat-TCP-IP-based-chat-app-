import socket
import threading
import json

HOST = "0.0.0.0"
PORT = 5000

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind((HOST, PORT))
server.listen()
print("Server running on port 5000...")

rooms = {}   # room_name -> {password, clients}
clients = {} # conn -> username

def broadcast(room, message, sender_conn=None):
    # FIX: Send to ALL clients including sender + add \n delimiter
    for client in rooms[room]["clients"]:
        try:
            client.send((message + "\n").encode())
        except:
            pass

def handle_client(conn, addr):
    print(f"Connected: {addr}")
    current_room = None
    username = None

    while True:
        try:
            data = conn.recv(4096).decode()
            if not data:
                break

            msg = json.loads(data)

            # CREATE ROOM
            if msg["type"] == "create_room":
                room = msg["room"]
                password = msg["password"]
                if room not in rooms:
                    rooms[room] = {"password": password, "clients": []}
                rooms[room]["clients"].append(conn)
                current_room = room
                username = msg["user"]
                clients[conn] = username
                print(f"{username} created room {room}")
                conn.send((json.dumps({
                    "type": "system",
                    "text": f"Room '{room}' created. You are connected."
                }) + "\n").encode())

            # JOIN ROOM
            elif msg["type"] == "join":
                room = msg["room"]
                password = msg["password"]
                if room in rooms and rooms[room]["password"] == password:
                    rooms[room]["clients"].append(conn)
                    current_room = room
                    username = msg["user"]
                    clients[conn] = username
                    print(f"{username} joined {room}")
                    broadcast(room, json.dumps({
                        "type": "system",
                        "text": f"{username} joined the room"
                    }))
                else:
                    conn.send((json.dumps({
                        "type": "error",
                        "text": "Wrong password or room not found"
                    }) + "\n").encode())

            # GROUP MESSAGE - FIX: broadcast to everyone including sender
            elif msg["type"] == "message":
                room = msg["room"]
                broadcast(room, json.dumps({
                    "type": "message",
                    "user": msg["user"],
                    "text": msg["text"]
                }))

            # PRIVATE MESSAGE (DM)
            elif msg["type"] == "dm":
                target_user = msg["to"]
                for client_conn, user in clients.items():
                    if user == target_user:
                        client_conn.send((json.dumps({
                            "type": "dm",
                            "user": msg["user"],
                            "text": msg["text"]
                        }) + "\n").encode())
                # Also confirm to sender
                conn.send((json.dumps({
                    "type": "dm",
                    "user": "You → " + target_user,
                    "text": msg["text"]
                }) + "\n").encode())

        except:
            break

    # DISCONNECT
    if current_room and current_room in rooms and conn in rooms[current_room]["clients"]:
        rooms[current_room]["clients"].remove(conn)
        broadcast(current_room, json.dumps({
            "type": "system",
            "text": f"{username} left the room"
        }))
    if conn in clients:
        print(f"{clients[conn]} disconnected")
        del clients[conn]
    conn.close()

# ACCEPT CONNECTIONS
while True:
    conn, addr = server.accept()
    threading.Thread(target=handle_client, args=(conn, addr)).start()
