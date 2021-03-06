package org.pl.android.drively.ui.chat.addgroup;

import org.pl.android.drively.ui.base.MvpView;

public interface AddGroupMvpView extends MvpView {
    void addRoomForUser(final String roomId, final int userIndex);

    void addRoomForUserFailure();

    void editGroupSuccess(String idGroup);

    void editGroupFailure();

    void onFailureGroupReference();

    void onSuccessDeleteGroupReference(String roomId, int userIndex);
}
