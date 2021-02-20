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

    public static void serverPrinter(User myUser) {
        try {
            byte[] data = new byte[1000];
            String message = "";
            DatagramPacket messageIn;
            String[] result;


            while (!message.equals("Server: !exit")) {
                messageIn = new DatagramPacket(data, data.length);
                serverSocket.receive(messageIn);
                message = new String(messageIn.getData()).trim();
                data = new byte[1000];
                result = message.split(" ", 3);

                //Hides messages from myUser
                if (!result[0].equals(myUser.getUsername() + ":")) {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException((IOException) e);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            // Closing everything...
            System.out.println("Server Listener closing...");
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    public static void chatPrinter(User myUser) {
        try {
            byte[] data = new byte[1000];
            String message = "";
            DatagramPacket dataOut;
            DatagramPacket messageIn;
            String[] result;

            // Listening loop, prints every message on the room
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                messageIn = new DatagramPacket(data, data.length);
                listenerSocket.receive(messageIn);
                message = new String(messageIn.getData()).trim();

                result = message.split(" ", 3);

                //Hides messages from myUser
                if (!result[0].equals(myUser.getUsername() + ":")) {
                    System.out.println(message);
                }

                data = new byte[1000]; // Cleans buffer
            }
        } catch (IOException e) {
            throw new UncheckedIOException((IOException) e);
        } catch (Exception e){
            e.printStackTrace();
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
            User myUser = new User(username, "1");

            //Server printer
            new Thread(() -> {
                serverPrinter(myUser);
            }).start();

            //Chat printer
            new Thread(() -> {
                chatPrinter(myUser);
            }).start();

            // Alerts user joined the server
            message = myUser.getUsername() + " joined room 1";
            data = message.getBytes();
            dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
            serverSocket.send(dataOut);
            data = new byte[1000];

            // Messager loop, sends until user leaves
            while (!message.equals(myUser.getUsername() + ": " + "!leave")) {
                message = myUser.getUsername() + ": " + input.nextLine();
                String[] result = message.split(" ", 3);
                data = message.getBytes();

                if (result[0].equals(myUser.getUsername() + ":") && result[1].equals("!join")) {
                    message = myUser.getUsername() + " joined room " + result[2];
                    data = message.getBytes();
                    dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
                    serverSocket.send(dataOut);

                    // Updates User current room and joins new group
                    listenerSocket.leaveGroup(Room.convertStringToInet(myUser.getCurrentRoomId()));
                    myUser.setCurrentRoomId(result[2]);
                    listenerSocket.joinGroup(Room.convertStringToInet(myUser.getCurrentRoomId()));
                } else if (result[0].equals(myUser.getUsername() + ":") && (result[1].equals("!rooms")) || (result[1].equals("!users") || (result[1].equals("!leave")))) {
                    data = message.getBytes();
                    dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
                    serverSocket.send(dataOut);
                } else {
                    dataOut = new DatagramPacket(data, data.length, Room.convertStringToInet(myUser.getCurrentRoomId()), roomPort);
                    listenerSocket.send(dataOut);
                }
                data = new byte[1000]; //Cleans the buffer
            }

            // Closes the listener
            message = myUser.getUsername() + ": !leave ";
            data = message.getBytes();
            dataOut = new DatagramPacket(data, data.length, Room.convertStringToInet(myUser.getCurrentRoomId()), roomPort);
            serverSocket.send(dataOut);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("App closing...");
        }
    }
}
