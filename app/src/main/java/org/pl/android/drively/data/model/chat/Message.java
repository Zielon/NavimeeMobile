package org.pl.android.drively.data.model.chat;

import android.support.annotation.NonNull;

import java.util.Comparator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Message implements Comparator<Message>, Comparable<Message> {
    public String idSender;
    public String idRoom;
    public String emailSender;
    public String nameSender;
    public String text;
    public long timestamp;
    public boolean deleted;

    @Override
    public int compare(Message a, Message b) {
        return (int) (a.timestamp - b.timestamp);
    }

    @Override
    public int compareTo(@NonNull Message message) {
        return (int) (this.timestamp - message.timestamp);
    }
}