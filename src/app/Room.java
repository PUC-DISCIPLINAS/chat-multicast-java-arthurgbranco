package app;

public class Room {
    private int roomPort;

    public Room(int roomPort){
        setRoomPort(roomPort);
    }

    public int getRoomPort() {
        return roomPort;
    }

    public void setRoomPort(int roomPort) {
        this.roomPort = roomPort;
    }
}
