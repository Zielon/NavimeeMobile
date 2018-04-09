package org.pl.android.drively.ui.finance.pages.calendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;

import javax.inject.Inject;

public class CalendarFragment extends BaseFinanceFragment implements CalendarMvpView {

    @Inject
    CalendarPresenter calendarPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = initializeRecyclerLayout(inflater, container);
        calendarPresenter.attachView(this);
        return view;
    }

}
