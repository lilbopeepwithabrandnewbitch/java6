package com.mycompany.mavenproject1;

import java.net.*;
import java.util.concurrent.*;

public class IntegralServer {
    private static final int SERVER_PORT = 5000;
    private static final int CLIENTS = 3;
    private static final int TIMEOUT_SECONDS = 10;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(CLIENTS);
        
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("Server works on port " + SERVER_PORT);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                
                executor.submit(() -> processRequest(packet, serverSocket));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static void processRequest(DatagramPacket packet, DatagramSocket serverSocket) {
        try {
            String request = new String(packet.getData(), 0, packet.getLength());
            String[] parts = request.split(",");
            double lower = Double.parseDouble(parts[0]);
            double upper = Double.parseDouble(parts[1]);
            double step = Double.parseDouble(parts[2]);

            System.out.println("Get: " + request);

            double result = new ParallelIntegral(lower, upper, step).calculate();
            String response = "RESULT," + result;
            
            DatagramPacket responsePacket = new DatagramPacket(
                response.getBytes(),
                response.getBytes().length,
                packet.getAddress(),
                packet.getPort()
            );
            
            serverSocket.send(responsePacket);
            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}