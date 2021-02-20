package object;

import java.net.InetAddress;

public class User {
    private String username;
    private String currentRoomId;

    public  User(String username, String currentRoomId){
        setUsername(username);
        setCurrentRoomId(currentRoomId);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    @Override
    public String toString(){
        return this.getUsername();
    }
}
