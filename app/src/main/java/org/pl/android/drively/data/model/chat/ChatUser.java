package org.pl.android.drively.data.model.chat;

public class ChatUser {

    public String name;
    public String email;
    public String avatar;
    public String id;
    public Status status;
    public Message message;

    public ChatUser() {
        status = new Status();
        status.isOnline = false;
        status.timestamp = 0;
        avatar = "DEFAULT";
    }
}
