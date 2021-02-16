package app;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void chatPrinter(int roomPort, String username) throws IOException {

        MulticastSocket listenerSocket = new MulticastSocket(roomPort);
        byte[] listenerBuffer = new byte[1000];
        InetAddress groupIp = InetAddress.getByName("228.0.0.4");
        listenerSocket.joinGroup(groupIp);

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

        //ip:porta (lister+client) -> 5001 (sala 1) ---> ip:5000
        //ip:5000


        // Closing everything...
        System.out.println("Listener closing...");
        listenerSocket.leaveGroup(groupIp);
        if(listenerSocket != null){
            listenerSocket.close();
        }
    }

    public static void main(String args[]) {

        MulticastSocket mSocket = null;
        Scanner input = new Scanner(System.in);

        System.out.print("Input your username: ");
        String username = input.nextLine();


        try {
            int roomPort = 5001;

            InetAddress groupIp = InetAddress.getByName("228.0.0.4");
            mSocket = new MulticastSocket(roomPort);
            mSocket.joinGroup(groupIp);

            //Chat printer
            new Thread(() -> {
                try {
                    chatPrinter(roomPort, username);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).start();

            byte[] data = null;
            String message = "";
            while (!message.equals(username + ": " + "!leave")){
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
            if (mSocket != null)
                mSocket.close();
            System.out.println("App closing...");
        }
    }
}