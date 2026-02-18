package br.com.chat.peer.chatpeer.model;


public record DiscoveredPeer(
        String id,        // ip:tcpPort
        String name,
        String ip,
        int tcpPort,
        long lastSeenMs
) {}
