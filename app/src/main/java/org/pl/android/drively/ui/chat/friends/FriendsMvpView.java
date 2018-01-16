package org.pl.android.drively.ui.chat.friends;

import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.User;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

/**
 * Created by Wojtek on 2018-01-11.
 */

public interface FriendsMvpView extends MvpView {


    void showError();

    void userNotFound();

    void friendInfoFound(int index, Friend friend);

    void userFound(User user);

    void listFriendFound(List<String> friendList);

    void listFriendNotFound();

    void addFriendSuccess(String idFriend);

    void addFriendFailure();

    void addFriendIsNotIdFriend();

    void onSuccessDeleteFriend(String idFriend);

    void onFailureDeleteFriend();
}
