package org.pl.android.navimee.ui.main;

import java.util.List;

import org.pl.android.navimee.data.model.Ribot;
import org.pl.android.navimee.ui.base.MvpView;

public interface MainMvpView extends MvpView {

    void showRibots(List<Ribot> ribots);

    void showRibotsEmpty();

    void showError();

}
