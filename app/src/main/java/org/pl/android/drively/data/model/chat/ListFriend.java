package org.pl.android.drively.data.model.chat;

import java.util.ArrayList;


public class ListFriend {
    private ArrayList<Friend> listFriend;

    public ListFriend() {
        listFriend = new ArrayList<>();
    }

    public ArrayList<Friend> getListFriend() {
        return listFriend;
    }

    public void setListFriend(ArrayList<Friend> listFriend) {
        this.listFriend = listFriend;
    }

    public Friend getById(String id) {
        for (Friend friend : listFriend) {
            if (id.equals(friend.id)) {
                return friend;
            }
        }
        return null;
    }
}
