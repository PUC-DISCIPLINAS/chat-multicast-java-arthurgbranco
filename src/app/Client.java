package app;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void chatPrinter(int roomPort, String username, MulticastSocket listenerSocket, InetAddress groupIp) {
        try {
            byte[] listenerBuffer = new byte[1000];
            String command = "";

            while (!command.equals(username + ": " + "!leave")) { // get messages from others in group
                DatagramPacket messageIn = new DatagramPacket(listenerBuffer, listenerBuffer.length);
                listenerSocket.receive(messageIn);
                command = new String(messageIn.getData()).trim();
                System.out.println(command);
                listenerBuffer = new byte[1000];
            }

            // Announce in chat user is leaving
            command = username + " left the room!";
            listenerBuffer = command.getBytes();
            DatagramPacket dataOut = new DatagramPacket(listenerBuffer, listenerBuffer.length, groupIp, roomPort);
            listenerSocket.send(dataOut);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            // Closing everything...
            System.out.println("Listener closing...");
            if (listenerSocket != null) {
                listenerSocket.close();
            }
        }
    }

    public static void main(String args[]) throws IOException {

        System.out.print("Input your username: ");
        Scanner input = new Scanner(System.in);
        String username = input.nextLine();

        int roomPort = 5001;
        final MulticastSocket mSocket = new MulticastSocket(roomPort);

        try {
            InetAddress groupIp = InetAddress.getByName("228.0.0.4");
            mSocket.joinGroup(groupIp);

            //Chat printer
            new Thread(() -> {
                chatPrinter(roomPort, username, mSocket, groupIp);
            }).start();

            byte[] data = null;
            String message = "";
            while (!message.equals(username + ": " + "!leave")) {
                message = username + ": " + input.nextLine();
                data = message.getBytes();
                DatagramPacket dataOut = new DatagramPacket(data, data.length, groupIp, roomPort);
                mSocket.send(dataOut);
                data = new byte[1000]; //Cleans the buffer
            }
            mSocket.leaveGroup(groupIp);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            System.out.println("App closing...");
        }
    }
}
