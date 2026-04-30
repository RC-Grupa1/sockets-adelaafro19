#!/usr/bin/env bash
# ============================================================
#  TCP Chat – build helper
#  Usage:
#    ./run.sh server        → start the server
#    ./run.sh client        → start a client
#    ./run.sh compile       → only compile
# ============================================================

SRC=src
BIN=bin

compile() {
    mkdir -p "$BIN"
    javac -encoding UTF-8 -d "$BIN" "$SRC"/ChatServer.java "$SRC"/ChatClient.java
    echo "Compilation successful → $BIN/"
}

case "$1" in
    compile)
        compile
        ;;
    server)
        compile && java -cp "$BIN" ChatServer
        ;;
    client)
        compile && java -cp "$BIN" ChatClient
        ;;
    *)
        echo "Usage: $0 {compile|server|client}"
        exit 1
        ;;
esac
