package org.pl.android.drively.ui.chat.group;

import android.content.Context;

import com.google.firebase.firestore.ListenerRegistration;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;

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
