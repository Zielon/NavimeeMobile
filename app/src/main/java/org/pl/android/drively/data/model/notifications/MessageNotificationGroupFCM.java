package org.pl.android.drively.data.model.notifications;

/**
 * Created by Wojtek on 2018-02-22.
 */

public class MessageNotificationGroupFCM extends MessageNotificationFCM {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
