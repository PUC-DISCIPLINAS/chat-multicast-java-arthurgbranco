package app;

import object.Room;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

    public static void eventPrinter(){

    }

    public static void main(String args[]) throws IOException {
        int roomPort = 5001;
        final MulticastSocket serverSocket = new MulticastSocket(roomPort);
        ArrayList<Room> rooms = new ArrayList<>();

        try{
            InetAddress serverIp = InetAddress.getByName("228.0.10.1");
            serverSocket.joinGroup(serverIp);

            //Event printer
            new Thread(() -> {
                eventPrinter();
            }).start();

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }finally {
            // Closing everything...
            System.out.println("Listener closing...");
            if (serverSocket != null) {
                serverSocket.close();
            }
        }

    }

}
