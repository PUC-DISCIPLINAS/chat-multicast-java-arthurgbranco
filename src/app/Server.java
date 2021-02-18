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

    public static ArrayList<Room> rooms;

    public static void serverCommandListener(int roomPort, MulticastSocket listenerSocket) {
        try {
            byte[] listenerBuffer = new byte[1000];
            String command = "";

            // Server starts with room 1
            rooms.add(new Room("1"));

            // Listening loop, prints every message on the room
            while (!command.equals("!exit")) {
                String[] result = command.split(" ", 2);
                if (result[0].equals("!create")) {
                    if(Server.rooms.stream().anyMatch(room -> room.getRoomId().equals("1"))){
                        Server.rooms.add(new Room(result[1]));
                    }else{
                        System.out.println("Room already exists");
                    }
                } else if (result[0].equals("!delete")) {
                    Server.rooms.removeIf(room -> room.getRoomId().equals(result[1]));
                } else {
                    DatagramPacket messageIn = new DatagramPacket(listenerBuffer, listenerBuffer.length);
                    listenerSocket.receive(messageIn);
                    command = new String(messageIn.getData()).trim();
                    System.out.println(command);
                    listenerBuffer = new byte[1000]; // Cleans buffer
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            // Closing everything...
            System.out.println("Server Command Listener closing...");
            if (listenerSocket != null) {
                listenerSocket.close();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        int roomPort = 5001;
        final MulticastSocket serverSocket = new MulticastSocket(roomPort);

        // TODO: Implement userCommunicationSocket logic
        final MulticastSocket userCommunicationSocket = new MulticastSocket(roomPort);

        Scanner input = new Scanner(System.in);

        try {
            InetAddress serverIp = InetAddress.getByName("228.0.10.1");
            serverSocket.joinGroup(serverIp);

            //Event printer
            new Thread(() -> {
                serverCommandListener(roomPort, serverSocket);
            }).start();

            byte[] data = null;
            String command = "";

            // Messager loop, sends until user leaves
            while (!command.equals("!exit")) {
                command = input.nextLine();
                data = command.getBytes();
                DatagramPacket dataOut = new DatagramPacket(data, data.length, serverIp, roomPort);
                serverSocket.send(dataOut);
                data = new byte[1000]; //Cleans the buffer
            }

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
