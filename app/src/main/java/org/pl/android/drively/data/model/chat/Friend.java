package org.pl.android.drively.data.model.chat;



public class Friend extends User{
    public String id;
    public String idRoom;

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
}
