package org.pl.android.drively.ui.chat;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

@ConfigPersistent
public class ChatPresenter extends BasePresenter<ChatMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public ChatPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(ChatMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }



}
