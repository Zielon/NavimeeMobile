package org.pl.android.drively.ui.chat.friends;

import android.graphics.Bitmap;

import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

public interface FriendsMvpView extends MvpView {

    void showError();

    void friendInfoFound(int index, Friend friend);

    void listFriendFound(List<String> friendList);

    void listFriendNotFound();

    void addFriendSuccess(String idFriend);

    void addFriendFailure();

    void addFriendIsNotIdFriend();

    void onSuccessDeleteFriend(String idFriend);

    void onFailureDeleteFriend();

    void onSuccessDeleteFriendReference(String idFriend);

    void onSetUserAvatarSuccess(Bitmap src);

    void onSetUserAvatarFailure();
}
