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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendModel that = (FriendModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
