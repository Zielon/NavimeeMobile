package org.pl.android.drively.ui.finance.pages.calendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;

public class CalendarFragment extends BaseFinanceFragment implements CalendarMvpView {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return container;
    }

}
