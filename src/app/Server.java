package app;

import object.Room;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

    static int roomPort = 5001;
    static MulticastSocket clientSocket;
    static ArrayList<Room> rooms = new ArrayList<>();

    static {
        try {
            clientSocket = new MulticastSocket(roomPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serverCommandListener(InetAddress clientIp) {
        try {
            byte[] data = new byte[1000];
            String message = "";
            StringBuilder finalStr = new StringBuilder("Server: ");
            DatagramPacket messageIn;
            DatagramPacket dataOut;

            // Listening loop, prints every message on the room
            while (!message.equals("!exit")) {
                messageIn = new DatagramPacket(data, data.length);
                clientSocket.receive(messageIn);
                message = new String(messageIn.getData()).trim();
                System.out.println("Debug, Message in: " + message); //TODO: remove
                String[] result = message.split(" ", 3);
                if (result[1].equals("!rooms")){
                    System.out.println("rooms received!");
                }else if (result[1].equals("!users")){
                    System.out.println("users received!");
                }else if (result[1].equals("!exit")){
                    message = "!exit";
                }
                data = new byte[1000]; // Cleans buffer
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            // Closing everything...
            System.out.println(rooms.toString()); // TODO: for debug reasons, remove later
            if (clientSocket != null) {
                clientSocket.close();
            }
            System.out.println("Server Command Listener closing...");
        }
    }

    public static void main(String args[]) throws IOException {

        Scanner input = new Scanner(System.in);

        try {
            // Server starts with room 1
            rooms.add(new Room("1"));

            InetAddress clientIp = InetAddress.getByName("228.0.10.1");
            clientSocket.joinGroup(clientIp);

            //Event printer
            new Thread(() -> {
                serverCommandListener(clientIp);
            }).start();

            byte[] data = null;
            String message = "";

            // Messager loop, sends until user leaves
            while (!message.equals("!exit")) {
                message = input.nextLine();
                String[] result = message.split(" ", 2);
                // Checks for commands and do specific actions
                if (result[0].equals("!create")) {
                    if (!Server.rooms.stream().anyMatch(room -> room.getRoomId().equals(result[1]))) {
                        Server.rooms.add(new Room(result[1]));
                        System.out.println("Room created!");
                    } else {
                        System.out.println("Room already exists");
                    }
                } else if (result[0].equals("!delete")) {
                    if (Server.rooms.removeIf(room -> room.getRoomId().equals(result[1]))) {
                        System.out.println("Room" + result[1] + " deleted!");
                    } else {
                        System.out.println("Room doesn't exist");
                    }
                }
            }

            message = "Server: !exit";
            data = message.getBytes();
            DatagramPacket dataOut = new DatagramPacket(data, data.length, clientIp, roomPort);
            clientSocket.send(dataOut);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            // Closing everything...
            System.out.println("Server closing...");
        }
    }
}
