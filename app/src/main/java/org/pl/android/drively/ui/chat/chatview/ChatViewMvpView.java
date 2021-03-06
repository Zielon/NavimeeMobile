package org.pl.android.drively.ui.chat.chatview;

import org.pl.android.drively.data.model.chat.Conversation;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.ui.base.MvpView;

import java.util.List;

public interface ChatViewMvpView extends MvpView {

    void addNewBatch(List<Message> messages);

    void addMessagesAtTheBeginning(List<Message> messages);

    void setAllMessagesLoaded(boolean allMessagesLoaded);

    Conversation getConversation();
}
