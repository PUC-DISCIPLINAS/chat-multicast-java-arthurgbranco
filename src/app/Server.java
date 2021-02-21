package app;

import object.Room;
import object.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Server {

    static int roomPort = 5001;
    static MulticastSocket clientSocket;
    static List<Room> rooms = new ArrayList<>();

    static {
        try {
            clientSocket = new MulticastSocket(roomPort);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
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
                String[] result = message.split(" ", 4);
                String username = result[0].substring(0, result[0].length());
                if(result[1].equals("joined") && result[2].equals("room")){
                    String roomId = result[3];
                    User user = new User(username, roomId);

                    for (Room r: rooms){
                        r.getConnectedUsers().removeIf(u -> u.getUsername().equals(username));
                    }

                    Optional<Room> room = rooms.stream().filter(r -> r.getRoomId().equals(roomId)).findFirst();
                    room.get().getConnectedUsers().add(user);
                }

                switch (result[1]){
                    case "!rooms":
                        message = "Server: " + rooms.toString();
                        data = message.getBytes();
                        dataOut = new DatagramPacket(data, data.length, clientIp, roomPort);
                        clientSocket.send(dataOut);
                        break;
                    case "!leave":
                        for (Room r: rooms){
                            r.getConnectedUsers().removeIf(u -> u.getUsername().equals(username));
                        }
                        message = username + " left the server!";
                        data = message.getBytes();
                        dataOut = new DatagramPacket(data, data.length, clientIp, roomPort);
                        clientSocket.send(dataOut);
                        break;
                    case "!exit":
                        message = "!exit";
                }
                data = new byte[1000]; // Cleans buffer
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e){
            e.printStackTrace();
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
