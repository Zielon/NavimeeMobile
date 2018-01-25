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
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
        message.emailSender = "";
        message.nameSender = "";
        avatar = "DEFAULT";
    }
}
