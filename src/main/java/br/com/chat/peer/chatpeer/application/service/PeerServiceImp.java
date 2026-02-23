package br.com.chat.peer.chatpeer.application.service;


import br.com.chat.peer.chatpeer.application.domain.model.PeerNode;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PeerServiceImp implements PeerServiceInterface {
    private final PeerNode peer = new PeerNode();

    @Override
    public PeerNode getPeer() {
        return peer;
    }

    @Override
    public void login(String name, Integer tcpPortOrNull) throws IOException {
        peer.start(name, tcpPortOrNull);
    }
    @Override
    public boolean privateMessage(  String peerId, String text){
        return  peer.privateMessage(peerId, text);
    }

    @Override
    public void connect(String host, int port) throws IOException {
        peer.connectToPeer(host, port);
    }

    @Override
    public void send(String text) {
        peer.broadcast(text);
    }

    @Override
    public void logout() {
        peer.stop();
    }
}
