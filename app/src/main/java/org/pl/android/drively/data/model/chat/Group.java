package org.pl.android.drively.data.model.chat;


public class Group extends Room {
    public String id;
    public ListFriend listFriend;

    public Group() {
        listFriend = new ListFriend();
    }
}
