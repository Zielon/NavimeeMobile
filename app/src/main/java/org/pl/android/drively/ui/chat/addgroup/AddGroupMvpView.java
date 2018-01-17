package org.pl.android.drively.ui.chat.addgroup;

import org.pl.android.drively.ui.base.MvpView;

/**
 * Created by Wojtek on 2018-01-17.
 */

public interface AddGroupMvpView extends MvpView {
    void addRoomForUser(final String roomId, final int userIndex);

    void addRoomForUserFailure();
}
