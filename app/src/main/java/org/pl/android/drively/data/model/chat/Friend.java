package org.pl.android.drively.data.model.chat;

import android.support.annotation.NonNull;

public class Friend extends ChatUser implements Comparable<Friend> {

    public String id;
    public String idRoom;
    public byte[] avatarBytes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Friend friend = (Friend) o;

        return id != null ? id.equals(friend.id) : friend.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(@NonNull Friend friend) {
        return name.toUpperCase().compareTo(friend.name.toUpperCase());
    }
}
