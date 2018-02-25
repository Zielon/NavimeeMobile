package org.pl.android.drively.data.model.chat;

public class PrivateMessage extends Message {
    public String idReceiver;

    public PrivateMessage() {
    }

    public PrivateMessage(String idReceiver) {
        this.idReceiver = idReceiver;
    }
}
