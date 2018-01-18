package org.pl.android.drively.ui.chat.friendsearch;

import org.pl.android.drively.data.model.chat.User;

import ir.mirrajabi.searchdialog.core.Searchable;

public class FriendModel implements Searchable {
    private String name;
    private String email;

    public FriendModel(User user) {
        name = user.name;
        email = user.email;
    }

    @Override
    public String getTitle() {
        return name;
    }

    public String getName(){
        return name;
    }

    public String getEmail() {
        return email;
    }
}
