package org.pl.android.drively.data.model.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Message {
    public String idSender;
    public String idRoom;
    public String emailSender;
    public String nameSender;
    public String text;
    public long timestamp;
    public boolean deleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (timestamp != message.timestamp) return false;
        if (deleted != message.deleted) return false;
        if (idSender != null ? !idSender.equals(message.idSender) : message.idSender != null)
            return false;
        if (idRoom != null ? !idRoom.equals(message.idRoom) : message.idRoom != null) return false;
        if (emailSender != null ? !emailSender.equals(message.emailSender) : message.emailSender != null)
            return false;
        if (nameSender != null ? !nameSender.equals(message.nameSender) : message.nameSender != null)
            return false;
        return text != null ? text.equals(message.text) : message.text == null;
    }

    @Override
    public int hashCode() {
        int result = idSender != null ? idSender.hashCode() : 0;
        result = 31 * result + (idRoom != null ? idRoom.hashCode() : 0);
        result = 31 * result + (emailSender != null ? emailSender.hashCode() : 0);
        result = 31 * result + (nameSender != null ? nameSender.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (deleted ? 1 : 0);
        return result;
    }
}