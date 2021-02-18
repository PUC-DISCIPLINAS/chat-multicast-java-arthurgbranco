package Objects;

import java.net.InetAddress;

public class User {
    private String username;
    private InetAddress currentRoomAddress;

    public User(){
        setUsername("noname");
    }

    public  User(String username, InetAddress address){
        setUsername(username);
        setCurrentRoomAddress(address);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public InetAddress getCurrentRoomAddress() {
        return currentRoomAddress;
    }

    public void setCurrentRoomAddress(InetAddress currentRoomAddress) {
        this.currentRoomAddress = currentRoomAddress;
    }
}
