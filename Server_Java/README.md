# TCP Chat – Java Sockets (UTF-8)

A minimal **multi-client TCP chat** written in Java.  
Every message is transmitted as UTF-8 text over a plain TCP stream on **port 12345**.

---

## Project structure

```
tcp-chat/
├── src/
│   ├── ChatServer.java   ← multi-client server (thread per client)
│   └── ChatClient.java   ← interactive console client
├── bin/                  ← compiled .class files (created on first build)
├── run.sh                ← convenience build + run script
└── README.md
```

---

## Requirements

| Tool | Version |
|------|---------|
| JDK  | 11 +    |

No external libraries needed.

---

## How to run

### 1 – Compile

```bash
chmod +x run.sh
./run.sh compile
```

Or manually:

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin src/ChatServer.java src/ChatClient.java
```

### 2 – Start the server

```bash
./run.sh server
# or: java -cp bin ChatServer
```

Output:
```
=== TCP Chat Server ===
Listening on port 12345 (UTF-8 encoding)
Capture traffic in Wireshark: tcp.port == 12345
Waiting for connections...
```

### 3 – Start one or more clients (each in its own terminal)

```bash
./run.sh client
# or: java -cp bin ChatClient
```

1. The server asks for a **username** – type it and press Enter.  
2. Chat freely.  
3. Type **`exit`** to close the connection gracefully.

---

## Protocol description

```
CLIENT → SERVER   username\n          (first message, username registration)
SERVER → CLIENT   welcome message\n
CLIENT → SERVER   any text\n          (chat message)
SERVER → CLIENT   [username]: text\n  (broadcast to all other clients)
CLIENT → SERVER   exit\n              (triggers graceful close)
SERVER → CLIENT   Goodbye!\n
                  ← TCP FIN/ACK sequence (Wireshark shows it clearly)
```

All strings are **UTF-8**, newline-delimited (`\n`).

---

## Capturing traffic in Wireshark

### Capture filter (before starting capture)

```
tcp port 12345
```

### Display filter (after capture)

```
tcp.port == 12345
```

### What you will see

| Event | TCP flag(s) |
|-------|-------------|
| Client connects | SYN → SYN-ACK → ACK (3-way handshake) |
| Message sent | PSH + ACK |
| `exit` typed | FIN + ACK from client, FIN + ACK from server |

### Reading the payload

1. Click any **PSH+ACK** packet.  
2. In the **Packet Bytes** pane at the bottom, you can read the raw UTF-8 text.  
3. Right-click the TCP layer → **Follow → TCP Stream** to see the entire conversation as text.

> **Tip:** Because the encoding is plain UTF-8 with no encryption, every message is readable in clear text inside Wireshark – perfect for protocol analysis.

---

## Key implementation details

| Feature | Implementation |
|---------|---------------|
| Encoding | `StandardCharsets.UTF_8` on every `InputStreamReader` / `OutputStreamWriter` |
| Multi-client | One daemon `Thread` per accepted `Socket` |
| Broadcast | `ConcurrentHashMap`-backed `Set<ClientHandler>` |
| Graceful exit | Client sends `exit`, server closes socket → TCP FIN exchange visible in Wireshark |
| Auto-flush | `PrintWriter` constructed with `autoFlush = true` |
