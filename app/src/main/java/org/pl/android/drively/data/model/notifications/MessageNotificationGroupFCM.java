package org.pl.android.drively.data.model.notifications;

public class MessageNotificationGroupFCM extends MessageNotificationFCM {
    private String roomName;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
