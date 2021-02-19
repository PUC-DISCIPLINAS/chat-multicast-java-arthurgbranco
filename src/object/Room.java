package object;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Room {
    private String roomId;
    private InetAddress roomAddress;
    private ArrayList<User> connectedUsers;

    public Room(String roomNumber) throws IOException {
        setRoomId(roomNumber);
        setRoomAddress(getRoomId());
        setConnectedUsers(new ArrayList<User>());
    }

    public InetAddress getRoomAddress() {
        return roomAddress;
    }


    private void setRoomAddress(String roomNumber) throws IOException {
            this.roomAddress = convertStringToInet(roomNumber);
    }

    // Converts room X to InetAddress 228.0.100.X
    public static InetAddress convertStringToInet(String roomNumber) throws IOException {
        InetAddress groupIp = null;
        if (Integer.parseInt(roomNumber) >= 1 && Integer.parseInt(roomNumber) <= 255){
            String addressString = "228.0.100." + roomNumber;
            groupIp = InetAddress.getByName(addressString);
        } else{
            throw new IOException("Invalid roomId (must be between 1 and 255");
        }
        return groupIp;
    }

    public ArrayList<User> getConnectedUsers() {
        return connectedUsers;
    }

    private void setConnectedUsers(ArrayList<User> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) throws IOException {
        this.roomId = roomId;
        setRoomAddress(roomId);
    }

    @Override
    public String toString(){
        return "Id: " + this.getRoomId() + " Address: " + this.getRoomAddress() + " Users: " + this.getConnectedUsers();
    }
}
