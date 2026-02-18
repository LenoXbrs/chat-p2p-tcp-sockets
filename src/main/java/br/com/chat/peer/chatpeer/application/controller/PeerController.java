package br.com.chat.peer.chatpeer.application.controller;



import br.com.chat.peer.chatpeer.application.domain.PeerNode;
import br.com.chat.peer.chatpeer.application.service.PeerServiceImp;
import br.com.chat.peer.chatpeer.application.service.PeerServiceInterface;
import br.com.chat.peer.chatpeer.model.request.ConnectRequest;
import br.com.chat.peer.chatpeer.model.request.LoginRequest;
import br.com.chat.peer.chatpeer.model.request.SendRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/peer")
public class PeerController {

    private final PeerServiceInterface peerService;

    public PeerController(PeerServiceImp peerService) {
        this.peerService = peerService;
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) throws IOException {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name é obrigatório"));
        }
        peerService.login(req.name().trim(), req.tcpPort());
        return ResponseEntity.ok(status());
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody ConnectRequest req) throws IOException {
        if (req == null || req.host() == null || req.host().isBlank() || req.port() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "host e port são obrigatórios"));
        }
        peerService.connect(req.host().trim(), req.port());
        return ResponseEntity.ok(status());
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody SendRequest req) {
        if (req == null || req.text() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "text é obrigatório"));
        }
        peerService.send(req.text());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(peerService.getPeer().getHistorySnapshot());
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        PeerNode p = peerService.getPeer();
        return ResponseEntity.ok(Map.of(
                "running", p.isRunning(),
                "name", p.getUserName(),
                "tcpPort", p.getTcpPort(),
                "connections", p.connectionsCount()
        ));
    }
    @GetMapping("/peers")
    public ResponseEntity<?> peers() {
        return ResponseEntity.ok(peerService.getPeer().discoveredPeers());
    }



    @PostMapping("/pm")
    public ResponseEntity<?> pm(@RequestBody Map<String, String> body) {
        String peerId = body.get("peerId");
        String text = body.get("text");
        if (peerId == null || peerId.isBlank() || text == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "peerId e text são obrigatórios"));
        }
        boolean ok = peerService.privateMessage(peerId.trim(), text);
        return ResponseEntity.ok(Map.of("ok", ok));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        peerService.logout();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
