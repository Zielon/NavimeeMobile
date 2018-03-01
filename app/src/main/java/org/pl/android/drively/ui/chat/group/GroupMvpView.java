package org.pl.android.drively.ui.chat.group;

import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

public interface GroupMvpView extends MvpView {

    void showError();

    void setGroupList(List<String> rooms);

    void getGroupError();

    void setGroupInfo(List<Room> rooms);

    void deleteGroupSuccess(Room room);

    void deleteGroupFailure();

    void onSuccessDeleteGroupReference(Room room, int index);

    void onFailureGroupReference();

    void onSuccessLeaveGroup(Room room);

    void onFailureLeaveGroup();

    void onSuccessLeaveGroupReference(Room room);
}
