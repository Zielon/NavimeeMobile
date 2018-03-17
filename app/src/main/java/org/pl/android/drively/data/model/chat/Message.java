package org.pl.android.drively.data.model.chat;

public abstract class Message {
    public String idSender;
    public String idRoom;
    public String emailSender;
    public String nameSender;
    public String text;
    public long timestamp;
    public boolean deleted;
}