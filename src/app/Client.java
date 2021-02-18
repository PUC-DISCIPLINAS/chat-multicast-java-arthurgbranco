package app;

import object.User;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void chatPrinter(int roomPort, MulticastSocket listenerSocket, MulticastSocket serverSocket, User myUser) {
        try {
            byte[] listenerBuffer = new byte[1000];
            String command = "";

            // Listening loop, prints every message on the room
            while (!command.equals(myUser.getUsername() + ": " + "!leave")) {
                DatagramPacket messageIn = new DatagramPacket(listenerBuffer, listenerBuffer.length);
                listenerSocket.receive(messageIn);
                command = new String(messageIn.getData()).trim();

                //Hiddes messages from myUser
                String[] result = command.split(" ", 2);
                if (!result[0].equals(myUser.getUsername() + ":")){
                    System.out.println(command);
                }
                listenerBuffer = new byte[1000]; // Cleans buffer
            }

            // Announce in chat user is leaving
            command = myUser + " left the room!";
            listenerBuffer = command.getBytes();
            DatagramPacket dataOut = new DatagramPacket(listenerBuffer, listenerBuffer.length, myUser.getCurrentRoomAddress(), roomPort);
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
        final MulticastSocket serverSocket = new MulticastSocket(roomPort);

        try {

            //Client starts by connecting to room 1
            InetAddress groupIp = InetAddress.getByName("228.0.100.1");
            mSocket.joinGroup(groupIp);

            InetAddress serverIp = InetAddress.getByName("228.0.10.1");
            mSocket.joinGroup(groupIp);

            User myUser = new User(username, groupIp);

            //Chat printer
            new Thread(() -> {
                chatPrinter(roomPort, mSocket, serverSocket, myUser);
            }).start();

            byte[] data = null;
            String message = "";

            // Messager loop, sends until user leaves
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                message = myUser.getUsername() + ": " + input.nextLine();
                data = message.getBytes();
                DatagramPacket dataOut = new DatagramPacket(data, data.length, groupIp, roomPort);
                mSocket.send(dataOut);
                data = new byte[1000]; //Cleans the buffer
            }
            mSocket.leaveGroup(groupIp); // TODO: Refactor, implement dynamic group joining and leaving
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            System.out.println("App closing...");
        }
    }
}
