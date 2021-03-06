package org.pl.android.drively.data.model.chat;

import java.util.ArrayList;


public class Conversation {
    private ArrayList<Message> listMessageData;

    public Conversation() {
        listMessageData = new ArrayList<>();
    }

    public ArrayList<Message> getListMessageData() {
        return listMessageData;
    }

    public Message tryGetMessage(int position) {
        try {
            return this.listMessageData.get(position);
        } catch (Exception e) {
        }
        return null;
    }
}
