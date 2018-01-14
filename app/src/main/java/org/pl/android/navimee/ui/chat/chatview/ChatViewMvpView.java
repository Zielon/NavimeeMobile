package org.pl.android.navimee.ui.chat.chatview;

import org.pl.android.navimee.data.model.chat.Message;
import org.pl.android.navimee.ui.base.MvpView;

import java.util.List;

public interface ChatViewMvpView extends MvpView {

    void roomChangesListerSet(List<Message> message);
}
