package app;

import object.Room;
import object.User;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    static InetAddress clientIp;
    static InetAddress serverIp;
    static MulticastSocket listenerSocket;
    static MulticastSocket serverSocket;
    static final int roomPort = 5001;

    static {
        try {
            listenerSocket = new MulticastSocket(new InetSocketAddress(roomPort));
            serverSocket = new MulticastSocket(new InetSocketAddress(roomPort));

            serverIp = InetAddress.getByName("228.0.10.1");
            clientIp = InetAddress.getByName("228.0.100.1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void chatPrinter(User myUser) {
        try {
            byte[] data = new byte[1000];
            String message = "";
            DatagramPacket dataOut;
            DatagramPacket messageIn;

            // Listening loop, prints every message on the room
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                messageIn = new DatagramPacket(data, data.length);
                listenerSocket.receive(messageIn);
                message = new String(messageIn.getData()).trim();

                String[] result = message.split(" ", 3);

                //Hides messages from myUser
                if (!result[0].equals(myUser.getUsername() + ":")){
                    System.out.println(message);
                }

                data = new byte[1000]; // Cleans buffer
            }
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
        byte[] data = new byte[1000];
        String message = null;
        DatagramPacket messageIn = null;
        DatagramPacket dataOut = null;

        try {

            //Client starts by connecting to room 1
            listenerSocket.joinGroup(clientIp);
            serverSocket.joinGroup(serverIp);

            //Client user
            User myUser = new User(username, clientIp);

            // Alerts user joined the server
            message = myUser.getUsername() + " joined the server!";
            data = message.getBytes();
            dataOut = new DatagramPacket(data, data.length, clientIp, roomPort);
            listenerSocket.send(dataOut);
            data = new byte[1000];

            //Chat printer
            new Thread(() -> {
                chatPrinter(myUser);
            }).start();

            // Messager loop, sends until user leaves
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                message = myUser.getUsername() + ": " + input.nextLine();
                String[] result = message.split(" ", 3);
                data = message.getBytes();


                if(result[0].equals(myUser.getUsername() + ":") && result[1].equals("!join")){
                    message = myUser.getUsername() + " joined room " + result[2];
                    data = message.getBytes();
                    dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
                    serverSocket.send(dataOut);

                    // Updates User current room and joins new group
                    listenerSocket.leaveGroup(myUser.getCurrentRoomAddress());
                    myUser.setCurrentRoomAddress(Room.convertStringToInet(result[2]));
                    listenerSocket.joinGroup(myUser.getCurrentRoomAddress());
                }else if (result[0].equals(myUser.getUsername() + ":") && (result[1].equals("!rooms")) || (result[1].equals("!users")) ) {
                    data = message.getBytes();
                    dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
                    serverSocket.send(dataOut);
                }else{
                    dataOut = new DatagramPacket(data, data.length, myUser.getCurrentRoomAddress(), roomPort);
                    listenerSocket.send(dataOut);
                }
                data = new byte[1000]; //Cleans the buffer
            }

            // Closes the listener
            message = myUser.getUsername() + ": !leave";
            data = message.getBytes();
            dataOut = new DatagramPacket(data, data.length, myUser.getCurrentRoomAddress(), roomPort);
            serverSocket.send(dataOut);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            System.out.println("App closing...");
        }
    }
}
