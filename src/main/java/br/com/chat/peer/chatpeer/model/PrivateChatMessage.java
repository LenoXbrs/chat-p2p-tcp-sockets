package br.com.chat.peer.chatpeer.model;

public record PrivateChatMessage(
        long timestamp,
        String from,
        String toPeerId,   // ip:tcpPort (ou connectionId se tu preferir)
        String text,
        boolean outgoing
) {}
