package org.pl.android.navimee.ui.chat.group;

import android.content.Context;

import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.injection.ConfigPersistent;
import org.pl.android.navimee.injection.PerActivity;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.ui.chat.friends.FriendsMvpView;

import javax.inject.Inject;

/**
 * Created by Wojtek on 2018-01-11.
 */
public class GroupPresenter extends BasePresenter<GroupMvpView> {

    private final DataManager mDataManager;

    private ListenerRegistration mListener;
    private Context mContext;


    @Inject
    public GroupPresenter(DataManager dataManager,@ActivityContext Context context) {
        mDataManager = dataManager;
        mContext = context;
    }

    @Override
    public void attachView(GroupMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


}
