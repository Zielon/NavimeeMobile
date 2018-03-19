package org.pl.android.drively.ui.chat.friendsearch;

import android.graphics.Bitmap;

import com.google.firebase.firestore.IgnoreExtraProperties;

import org.pl.android.drively.data.model.chat.ChatUser;

import ir.mirrajabi.searchdialog.core.Searchable;

@IgnoreExtraProperties
public class FriendModel implements Searchable {
    private String name;
    private String email;
    private String id;
    private String avatar;
    private Bitmap avatarImage;

    public FriendModel(ChatUser chatUser) {
        name = chatUser.name;
        email = chatUser.email;
        id = chatUser.id;
        avatar = chatUser.avatar;
    }

    @Override
    public String getTitle() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendModel that = (FriendModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public Bitmap getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(Bitmap avatarImage) {
        this.avatarImage = avatarImage;
    }
}
