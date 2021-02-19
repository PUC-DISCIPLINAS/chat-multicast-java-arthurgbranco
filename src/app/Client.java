package app;

import object.Room;
import object.User;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static MulticastSocket listenerSocket;
    public static MulticastSocket serverSocket;
    public static final int roomPort = 5001;

    static {
        try {
            listenerSocket = new MulticastSocket(new InetSocketAddress(roomPort));
            serverSocket = new MulticastSocket(new InetSocketAddress(roomPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void chatPrinter(User myUser) {
        try {

            byte[] data = new byte[1000];
            String message = "";
            DatagramPacket dataOut;

            // Listening loop, prints every message on the room
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                DatagramPacket messageIn = new DatagramPacket(data, data.length);
                listenerSocket.receive(messageIn);
                message = new String(messageIn.getData()).trim();

                // TODO: migrate this section to messager loop
                String[] result = message.split(" ", 3);
                if(result[0].equals(myUser.getUsername() + ":") && result[1].equals("!join")){
                    message = myUser.getUsername() + " joined room " + result[2];
                    data = message.getBytes();
                    dataOut = new DatagramPacket(data, data.length, myUser.getCurrentRoomAddress(), roomPort);
                    listenerSocket.send(dataOut);

                    // Updates User current room and joins new group
                    listenerSocket.leaveGroup(myUser.getCurrentRoomAddress());
                    myUser.setCurrentRoomAddress(Room.convertStringToInet(result[2]));
                    listenerSocket.joinGroup(myUser.getCurrentRoomAddress());
                }

                //Hides messages from myUser
                if (!result[0].equals(myUser.getUsername() + ":")){
                    System.out.println(message);
                }

                data = new byte[1000]; // Cleans buffer
            }

            // Announce in chat user is leaving
            message = myUser.getUsername() + " left the room!";
            data = message.getBytes();
            dataOut = new DatagramPacket(data, data.length, myUser.getCurrentRoomAddress(), roomPort);
            listenerSocket.send(dataOut);
        } catch (IOException e) {
            throw new UncheckedIOException((IOException) e);
        } finally {
            // Closing everything...
            System.out.println("Listener closing...");
            if (listenerSocket != null) {
                listenerSocket.close();
            }
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {

        System.out.print("Input your username: ");
        Scanner input = new Scanner(System.in);
        String username = input.nextLine();

        try {

            //Client starts by connecting to room 1
            InetAddress groupIp = InetAddress.getByName("228.0.100.1");
            listenerSocket.joinGroup(groupIp);

            InetAddress serverIp = InetAddress.getByName("228.0.10.1");
            serverSocket.joinGroup(groupIp);

            //Client user
            User myUser = new User(username, groupIp);

            //Chat printer
            new Thread(() -> {
                chatPrinter(myUser);
            }).start();

            // Alerts user joined the server
            byte[] data = null;
            String message = myUser.getUsername() + " joined the server!";
            data = message.getBytes();
            DatagramPacket dataOut = new DatagramPacket(data, data.length, groupIp, roomPort);
            listenerSocket.send(dataOut);
            data = new byte[1000];

            //TODO: transform in method that checks if server is initialized
//            while (Server.rooms == null) { // does not work, needs to exchange data with socket
//                System.out.println("Waiting for server initialization...");
//                System.out.println(Server.rooms);
//                Thread.sleep(5000);
//            }
//            System.out.print("\033[H\033[2J");
//            System.out.flush();

            // Messager loop, sends until user leaves
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                message = myUser.getUsername() + ": " + input.nextLine();
                data = message.getBytes();
                dataOut = new DatagramPacket(data, data.length, groupIp, roomPort);
                listenerSocket.send(dataOut);
                data = new byte[1000]; //Cleans the buffer
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            System.out.println("App closing...");
        }
    }
}
