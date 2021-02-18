package object;

import java.io.IOException;
import java.net.InetAddress;
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
        if (Integer.parseInt(roomNumber) >= 1 && Integer.parseInt(roomNumber) <= 255){
            String addressString = "228.0.100." + roomNumber;
            InetAddress groupIp = InetAddress.getByName(addressString);

            this.roomAddress = groupIp;
        } else{
            throw new IOException("Invalid roomId (must be between 1 and 255");
        }
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
}
