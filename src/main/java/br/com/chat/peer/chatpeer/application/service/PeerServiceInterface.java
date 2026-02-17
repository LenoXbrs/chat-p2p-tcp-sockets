package br.com.chat.peer.chatpeer.application.service;

import br.com.chat.peer.chatpeer.application.domain.PeerNode;

import java.io.IOException;

public interface PeerServiceInterface {

    PeerNode getPeer();

    void login(String name, Integer tcpPortOrNull) throws IOException;

    void connect(String host, int port) throws IOException;

    void send(String text);

    void logout();
}
