package com.mycompany.mavenproject1;

import java.net.*;

public class IntegralClient {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            System.out.println("Client works");
            
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String request = new String(packet.getData(), 0, packet.getLength());
                String[] parts = request.split(",");
                double lower = Double.parseDouble(parts[0]);
                double upper = Double.parseDouble(parts[1]);
                double step = Double.parseDouble(parts[2]);
                
                System.out.printf("Integrate %.2f to %.2f, step %.2f%n", lower, upper, step);
                
                double result = new ParallelIntegral(lower, upper, step).calculate();
                String response = String.valueOf(result);
                
                DatagramPacket responsePacket = new DatagramPacket(
                    response.getBytes(),
                    response.getBytes().length,
                    packet.getAddress(),
                    packet.getPort()
                );
                
                socket.send(responsePacket);
                System.out.println("Result: " + result);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}