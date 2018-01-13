package org.pl.android.navimee.ui.chat.friends;

import org.pl.android.navimee.data.model.chat.User;
import org.pl.android.navimee.ui.base.MvpView;

/**
 * Created by Wojtek on 2018-01-11.
 */

public interface FriendsMvpView extends MvpView {


    void showError();

    void userNotFound();

    void userFound(User user);

}
