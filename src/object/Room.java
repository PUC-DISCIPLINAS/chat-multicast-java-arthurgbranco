package object;

import java.net.InetAddress;
import java.util.ArrayList;

public class Room {
    private InetAddress roomAddress;
    private ArrayList<User> connectedUsers;

    public Room(InetAddress roomAddress){
        setRoomAddress(roomAddress);
    }

    public InetAddress getRoomAddress() {
        return roomAddress;
    }

    public void setRoomAddress(InetAddress roomAddress) {
        this.roomAddress = roomAddress;
    }

    public ArrayList<User> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(ArrayList<User> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }
}
