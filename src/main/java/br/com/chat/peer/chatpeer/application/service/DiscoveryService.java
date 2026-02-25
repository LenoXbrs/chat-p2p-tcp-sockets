package br.com.chat.peer.chatpeer.application.service;


import br.com.chat.peer.chatpeer.model.DiscoveredPeer;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscoveryService {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final String groupIp = "230.0.0.1";
    private final int groupPort = 4446;
    private LanDiscovery lanDiscovery;
    private MulticastSocket socket;
    private InetAddress group;

    private volatile String myName;
    private volatile int myTcpPort;

    private final Map<String, DiscoveredPeer> peers = new ConcurrentHashMap<>();

    public Collection<DiscoveredPeer> listPeers() {
        // limpa peers muito antigos
        long now = System.currentTimeMillis();
        peers.values().removeIf(p -> now - p.lastSeenMs() > 8000);
        return peers.values();
    }

    public void start(String myName, int myTcpPort) throws Exception {
        if (running.get()) return;

        this.myName = myName;
        this.myTcpPort = myTcpPort;

        group = InetAddress.getByName(groupIp);


        socket = new MulticastSocket(null);
        socket.setReuseAddress(true);// multiplas instancias
        socket.bind(new InetSocketAddress(groupPort));

        socket.joinGroup(group);

        running.set(true);

        new Thread(this::listenLoop, "mc-listen").start();
        new Thread(this::announceLoop, "mc-announce").start();
    }

    public void stop() {
        running.set(false);
        peers.clear();
        try {
            if (socket != null) {
                socket.leaveGroup(group);
                socket.close();
            }
        } catch (Exception ignored) {}
    }

    private void announceLoop() {
        while (running.get()) {
            try {
                String msg = "DISCOVER|" + myName + "|" + myTcpPort;
                byte[] data = msg.getBytes(StandardCharsets.UTF_8);

                DatagramPacket p = new DatagramPacket(data, data.length, group, groupPort);
                socket.send(p);

                Thread.sleep(2000);
            } catch (Exception ignored) {}
        }
    }

    private void listenLoop() {
        byte[] buf = new byte[1024];

        while (running.get()) {
            try {
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                socket.receive(p);

                String ip = p.getAddress().getHostAddress();
                String msg = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);

                String[] parts = msg.split("\\|");
                if (parts.length < 3) continue;

                if (!"DISCOVER".equals(parts[0])) continue;

                String name = parts[1];
                int tcpPort = Integer.parseInt(parts[2]);

                // ignora a si mesmo (mesmo tcpPort)
                if (tcpPort == myTcpPort) continue;

                String id = ip + ":" + tcpPort;

                peers.put(id, new DiscoveredPeer(id, name, ip, tcpPort, System.currentTimeMillis()));

            } catch (Exception ignored) {}
        }
    }
}
