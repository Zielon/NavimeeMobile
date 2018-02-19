package org.pl.android.drively.data.model;

public class RoomMember {
    private String memberId;
    private boolean notification;

    public RoomMember() {
        this.notification = true;
    }

    public RoomMember(String memberId) {
        this();
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    @Override
    public boolean equals(Object obj) {
        return this.memberId.equals(((RoomMember) obj).getMemberId());
    }
}
