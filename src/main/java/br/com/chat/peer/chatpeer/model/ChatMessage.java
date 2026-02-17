package br.com.chat.peer.chatpeer.model;

public record ChatMessage(long timestamp, String from, String text, boolean outgoing) {

}
