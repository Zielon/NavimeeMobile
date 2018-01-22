package org.pl.android.drively.data.model.chat;

import org.pl.android.drively.data.model.User;

public class ChatUser extends User {
    public Status status;
    public Message message;

    public ChatUser(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
