package br.com.chat.peer.chatpeer.application.service;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class LanDiscovery {

    private static final int DISCOVERY_PORT = 45678;
    private volatile boolean running;

    // callback: (ip, tcpPort)
    private final BiConsumer<String, Integer> onPeer;

    public LanDiscovery(BiConsumer<String, Integer> onPeer) {
        this.onPeer = onPeer;
    }

    public void start(String myName, int myTcpPort) {
        running = true;

        new Thread(() -> senderLoop(myName, myTcpPort), "udp-announce").start();
        new Thread(this::listenerLoop, "udp-listen").start();
    }

    public void stop() {
        running = false;
    }

    private void senderLoop(String myName, int myTcpPort) {
        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setBroadcast(true);

            while (running) {
                String payload = "PEER|" + myName + "|" + myTcpPort;
                byte[] data = payload.getBytes(StandardCharsets.UTF_8);

                // broadcast geral
                DatagramPacket p = new DatagramPacket(
                        data, data.length,
                        InetAddress.getByName("255.255.255.255"),
                        DISCOVERY_PORT
                );

                sock.send(p);

                Thread.sleep(1200);
            }
        } catch (Exception ignored) {
        }
    }

    private void listenerLoop() {
        byte[] buf = new byte[512];

        try (DatagramSocket sock = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
            sock.setBroadcast(true);

            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                sock.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                String[] parts = msg.split("\\|");
                if (parts.length < 3) continue;
                if (!"PEER".equals(parts[0])) continue;

                String ip = packet.getAddress().getHostAddress();
                int tcpPort;
                try { tcpPort = Integer.parseInt(parts[2]); }
                catch (Exception e) { continue; }

                onPeer.accept(ip, tcpPort);
            }
        } catch (IOException ignored) {
        }
    }
}