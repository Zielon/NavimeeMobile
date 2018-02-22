package org.pl.android.drively.data.model.notifications;

/**
 * Created by Wojtek on 2018-02-22.
 */

public class MessageNotificationGroupFCM extends MessageNotificationFCM {
    private String roomName;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
