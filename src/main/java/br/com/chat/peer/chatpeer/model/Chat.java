package br.com.chat.peer.chatpeer.model;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Chat {

    private String userName;
    private ServerSocket serverSocket;
    private List<Socket> connections = new ArrayList<>();

    public Chat(String userName, int port){
        this.userName = userName;
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("O Peer "+ userName+ "está ouvindo na port :"+ port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void start(){
        new  Thread(this::listenForConnections).start();
        new Thread(this::listenForUserinput).start();
    }

    private void listenForUserinput() {
        try {
            BufferedReader userInput = new BufferedReader(
                    new InputStreamReader(System.in));
            while (true){

                String mensagem = userInput.readLine();
                broadcastMessage(mensagem);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastMessage(String mensagem) {
        for (Socket socket: connections){
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(),true);
                out.println(userName +" :" +mensagem);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void listenForConnections() {
        while (true){
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                new Thread(()-> handleConection(socket)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleConection(Socket socket) {

        try {


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String mensagem;
            while ((mensagem = in.readLine())!=null){
                System.out.println("mensagem :"+mensagem);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void connectionToPeer(String host, int port){

        try {
            Socket socket = new Socket(host,port);

            connections.add(socket);
            new Thread(() -> handleConection(socket)).start();
            System.out.println("Conectado a um peer em : "+host+ ":"+port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO : CRIAR PEERS / ALTERAR CONECTAR / ENVIAR MENSAGEM
    public static void main(String[] args) throws IOException {



        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o nome do usuario: ");
        String userName = scanner.nextLine();

        System.out.println("Digite a porta para escutar: ");
        int port = scanner.nextInt();
        scanner.nextLine();

        Chat peer = new Chat(userName,port);
        peer.start();

        System.out.println("Deseja conectar a outro peer? (s/n): ");
        String resposta = scanner.nextLine();

        if (resposta.equalsIgnoreCase("s")){
            System.out.println(" Digite endereço do outro host: ");
            String peerHost = scanner.nextLine();

            System.out.println("Digite a porta de outro peer :");
            int peerPort = scanner.nextInt();


            peer.connectionToPeer(peerHost,peerPort);




        }
        scanner.close();
    }
}
