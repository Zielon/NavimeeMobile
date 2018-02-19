package org.pl.android.drively.data.model.chat;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import org.pl.android.drively.data.model.RoomMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Room {
    @Exclude
    private List<RoomMember> members;
    private String admin;
    private String name;

    public Room() {
        this.members = new ArrayList<>();
    }

    public Room(String admin, String name, List<RoomMember> members) {
        this.members = members;
        this.admin = admin;
        this.name = name;
    }

    public List<RoomMember> getMembers() {
        return members;
    }

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

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("admin", this.admin);
        map.put("name", this.name);
        return map;
    }
}
