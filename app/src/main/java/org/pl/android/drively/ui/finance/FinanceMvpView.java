package org.pl.android.drively.ui.finance;

import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.tab.TabMvpView;

public interface FinanceMvpView extends TabMvpView {

    void goToDrivelyChat(Room room);

    void showNoDataLabel();
}
