package br.com.chat.peer.chatpeer.application.domain;


import br.com.chat.peer.chatpeer.application.service.DiscoveryService;
import br.com.chat.peer.chatpeer.model.ChatMessage;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerNode {

    private final ObjectMapper mapper = new ObjectMapper();
    private final DiscoveryService discovery = new DiscoveryService();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile String userName = "anon";
    private volatile int tcpPort = -1;

    private ServerSocket serverSocket;

    private final Map<String, PeerConnection> connections = new ConcurrentHashMap<>();

    private final List<ChatMessage> history = Collections.synchronizedList(new ArrayList<>());

    // ======== lifecycle ========

    public synchronized void start(String userName, Integer desiredPortOrNull) throws IOException {
        if (running.get()) return;

        this.userName = userName;

        // porta livre automática se null
        this.serverSocket = (desiredPortOrNull == null)
                ? new ServerSocket(0)
                : new ServerSocket(desiredPortOrNull);

        this.tcpPort = serverSocket.getLocalPort();
        running.set(true);

        new Thread(this::acceptLoop, "accept-loop").start();

        System.out.println("[peer] '" + this.userName + "' ouvindo TCP na porta " + this.tcpPort);
    }

    public synchronized void stop() {
        if (!running.get()) return;
        running.set(false);


        try { serverSocket.close(); } catch (IOException ignored) {}


        for (PeerConnection c : connections.values()) {
            try { c.send("BYE|" + userName); } catch (Exception ignored) {}
            c.close();
        }
        history.clear();
        connections.clear();

        System.out.println("[peer] parado");
    }

    public boolean isRunning() {
        return running.get();
    }
    public String getUserName() {

        return userName;
    }
    public int getTcpPort() {

        return tcpPort;
    }
    public int connectionsCount() {

        return connections.size();
    }

    public List<ChatMessage> getHistorySnapshot() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    // ======== TCP accept + connect ========

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                PeerConnection conn = new PeerConnection(socket);
                register(conn);

                new Thread(() -> readLoop(conn), "conn-" + conn.id()).start();

                // handshake
                conn.send("HELLO|" + userName + "|" + tcpPort);

            } catch (IOException e) {
                if (running.get()) {
                    System.out.println("[peer] erro accept: " + e.getMessage());
                }
            }
        }
    }

    public void connectToPeer(String host, int port) throws IOException {
        if (!running.get()) throw new IllegalStateException("Peer não iniciado. Faça /login primeiro.");

        Socket socket = new Socket(host, port);
        PeerConnection conn = new PeerConnection(socket);
        register(conn);

        new Thread(() -> readLoop(conn), "conn-" + conn.id()).start();

        conn.send("HELLO|" + userName + "|" + tcpPort);
        System.out.println("[peer] conectado em " + host + ":" + port);
    }

    private void register(PeerConnection conn) {
        connections.put(conn.id(), conn);
    }

    private void readLoop(PeerConnection conn) {
        try {
            String line;
            while ((line = conn.in().readLine()) != null) {
                onLineReceived(conn, line);
            }
        } catch (IOException ignored) {
        } finally {
            connections.remove(conn.id());
            conn.close();
        }
    }



    private void onLineReceived(PeerConnection conn, String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length == 0) return;

        switch (parts[0]) {
            case "HELLO" -> {
                if (parts.length >= 3) {
                    conn.setRemoteName(parts[1]);
                    System.out.println("[peer] " + conn.getRemoteName() + " entrou (" + conn.remoteIp() + ":" + parts[2] + ")");
                }
            }
            case "MSG" -> {
                if (parts.length >= 4) {
                    long ts = safeParseLong(parts[1]);
                    String from = parts[2];
                    String text = parts[3];

                    history.add(new ChatMessage(ts, from, text, false));
                    System.out.println(from + ": " + text);
                }
            }

            case "PRIV" -> {
                if (parts.length >= 4) {
                    long ts = safeParseLong(parts[1]);
                    String from = parts[2];
                    String text = parts[3];

                    history.add(new ChatMessage(ts, from, "[priv] " + text, false));
                    System.out.println("[priv] " + from + ": " + text);
                }
            }

            case "BYE" -> {
                System.out.println("[peer] " + conn.getRemoteName() + " saiu");
                connections.remove(conn.id());
                conn.close();
            }
            default -> {
                // ignora
            }
        }
    }

    private long safeParseLong(String s) {
        try {
            return Long.parseLong(s);
        }
        catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    // ======== broadcast ========

    public void broadcast(String text) {
        if (!running.get()) throw new IllegalStateException("Peer não iniciado. Faça /login primeiro.");

        long ts = System.currentTimeMillis();
        history.add(new ChatMessage(ts, userName, text, true));

        String line = "MSG|" + ts + "|" + userName + "|" + text;

        for (PeerConnection c : connections.values()) {
            try { c.send(line); }
            catch (Exception ignored) {}
        }
    }

    /*
    id: endereçoip + porta
    text:mensagem
     */
    public boolean privateMessage(String id, String text) {
        if (!running.get()) throw new IllegalStateException("Peer não iniciado. Faça /login primeiro.");

        PeerConnection p = connections.get(id);
        if (p == null) return false;

        long ts = System.currentTimeMillis();
        history.add(new ChatMessage(ts, userName, "[priv->" + id + "] " + text, true));

        String line = "PRIV|" + ts + "|" + userName + "|" + text;

        try {
            p.send(line);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}






