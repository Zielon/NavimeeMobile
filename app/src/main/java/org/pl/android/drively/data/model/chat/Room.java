package org.pl.android.drively.data.model.chat;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Room {

    private List<RoomMember> members;
    private String admin;
    private String name;
    private String id;
    private boolean editable;

    public Room() {
        this.members = new ArrayList<>();
    }

    public Room(String roomId, String admin, String name, List<RoomMember> members) {
        this.members = members;
        this.admin = admin;
        this.name = name;
        this.id = roomId;
    }

    @Exclude
    public List<RoomMember> getMembers() {
        return members;
    }

    @Exclude
    public void setMembers(List<RoomMember> members) {
        this.members = members;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
