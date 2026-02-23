package br.com.chat.peer.chatpeer.application.domain.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PeerConnection {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile String remoteName = "unknown";

    PeerConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    String id() {

        return remoteIp() + ":" + socket.getPort();
    }

    String remoteIp() {

        return socket.getInetAddress().getHostAddress();
    }

    BufferedReader in() {
        return in; }

    void setRemoteName(String name) {
        this.remoteName = name;
    }
    String getRemoteName() {
        return remoteName;
    }

    synchronized void send(String line) {
        out.println(line);
    }

    void close() {

        try { socket.close();
        } catch (IOException ignored) {

        }
    }
}
