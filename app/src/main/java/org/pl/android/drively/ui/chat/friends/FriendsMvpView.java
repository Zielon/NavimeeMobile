package org.pl.android.drively.ui.chat.friends;

import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

public interface FriendsMvpView extends MvpView {

    void showError();

    void allFriendsFound();

    void addFriendInfo(Friend friend);

    void listFriendFound(List<String> friendList);

    void listFriendNotFound();

    void addFriendSuccess(String idFriend);

    void addFriendFailure();

    void onFailureDeleteFriend();

    void onSuccessDeleteFriend(String idFriend);

}
