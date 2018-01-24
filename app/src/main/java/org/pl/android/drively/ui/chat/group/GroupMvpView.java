package org.pl.android.drively.ui.chat.group;

import org.pl.android.drively.data.model.chat.Group;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

/**
 * Created by Wojtek on 2018-01-11.
 */

public interface GroupMvpView extends MvpView {

    void showError();

    void setGroupList(List<String> rooms);

    void getGroupError();

    void setGroupInfo(int groupIndex, Room room);

    void deleteGroupSuccess(Group group);

    void deleteGroupFailure();

    void onSuccessDeleteGroupReference(Group group, int index);

    void onFailureGroupReference();

    void onSuccessLeaveGroup(Group group);

    void onFailureLeaveGroup();

    void onSuccessLeaveGroupReference(Group group);
}
