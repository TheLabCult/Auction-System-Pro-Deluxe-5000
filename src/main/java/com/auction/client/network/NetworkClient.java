package com.auction.client.network;

import com.auction.shared.network.Request;
import com.auction.shared.network.Response;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//dung luong chay ngam de lang nghe server
public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private NetworkClient() {}
    public static synchronized NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }
    //khoi tao ket noi toi server
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to server");
        startListening();
    }
    //gui 1 request len server (dc goi tu cac controller cua javafx)
    public synchronized void sendRequest(Request request) {
        try {
            if (out != null) {
                out.writeObject(request);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    private void startListening() {
        Thread listeningThread = new Thread(() -> {
            try {
                while (true) {
                    //block cho den khi server gui 1 object ve
                    Response response = (Response) in.readObject();
                    //phan luong xu ly response nhan dc
                    handleServerResponse(response);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Disconnected from server");
            }
        });
        listeningThread.setDaemon(true); //tu chet theo khi ng dung tat cua so app javafx di
        listeningThread.start();
    }
    //xu ly goi tin tra ve
    private void handleServerResponse(Response response) {
        //moi thao tac lquan den javafx bat buoc phai nam trong Platform.runLater()
        Platform.runLater(() -> {
            String status = response.getStatus();
            String message = response.getMessage();

            //xu ly cac loai thong bao dua vao status
            if ("SUCCESS".equals(status)) {
                System.out.println("SUCCESS: " + message);
            }
            else if ("ERROR".equals(status)) {
                System.out.println("ERROR: " + message);
            }
            else if ("REALTIME_UPDATE".equals(status)) {
                System.out.println("Server response: " + message);
            }
        });
    }
}
