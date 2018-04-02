package org.pl.android.drively.ui.finance;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gigamole.navigationtabstrip.NavigationTabStrip;

import org.pl.android.drively.R;
import org.pl.android.drively.common.SheetLayout;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.finance.form.add.AddFinanceActivity;
import org.pl.android.drively.ui.finance.pages.BaseFinanceFragment;
import org.pl.android.drively.ui.finance.pages.calendar.CalendarFragment;
import org.pl.android.drively.ui.finance.pages.daily.DailyFragment;
import org.pl.android.drively.ui.finance.pages.monthly.MonthlyFragment;
import org.pl.android.drively.ui.finance.pages.weekly.WeeklyFragment;
import org.pl.android.drively.ui.finance.pages.yearly.YearlyFragment;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java8.util.Optional;
import lombok.Getter;
import mehdi.sakout.fancybuttons.FancyButton;

import static org.pl.android.drively.ui.finance.pages.TopLeftPanelHelper.determineDateElementToChange;
import static org.pl.android.drively.ui.finance.pages.TopLeftPanelHelper.determineLeftPanelLabel;

public class FinanceFragment extends BaseTabFragment implements FinanceMvpView {

    private static final int ADD_FINANCE_REQUEST_CODE = 997;

    @Inject
    FinancePresenter financePresenter;

    private MaterialDialog popup;

    private MainActivity context;

    public static FinanceFragment newInstance() {
        return new FinanceFragment();
    }

    @BindView(R.id.no_data_label)
    TextView noDataLabel;

    @BindView(R.id.add_finance_button)
    FancyButton addFinanceButton;

    @BindView(R.id.reveal_animation_sheet)
    SheetLayout revealAnimation;

    @BindView(R.id.finance_navigation)
    NavigationTabStrip navigationTabStrip;

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    @BindView(R.id.date_change_label)
    TextView dateChangeLabel;

    @BindView(R.id.finance_sum)
    TextView financeSum;

    BaseFinanceFragment selectedFragment;

    @Getter
    private Set<Finance> alreadyLoadedData;

    @Getter
    protected Calendar selectedPanelDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && actionBar.getCustomView() != null) {
            actionBar.hide();
        }
        context = (MainActivity) this.getActivity();
        alreadyLoadedData = new TreeSet<>((finance1, finance2) -> finance1.getDate().compareTo(finance2.getDate()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.finance_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        financePresenter.attachView(this);
        initializeView();
        initializeRevealAnimation();
        determineFragmentReplacement(0);
        return fragmentView;
    }

    private void initializePanel() {
        selectedPanelDate = Calendar.getInstance();
        selectedFragment.setSelectedPanelDate(selectedPanelDate);
        dateChangeLabel.setText(determineLeftPanelLabel(selectedFragment, selectedPanelDate.getTime()));
    }

    private void initializeView() {
        navigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
                determineFragmentReplacement(index);
            }

            @Override
            public void onEndTabSelected(String title, int index) {
            }
        });
        navigationTabStrip.setTabIndex(0);
    }

    private void determineFragmentReplacement(int position) {
        switch (position) {
            case 0: {
                selectedFragment = new DailyFragment();
                break;
            }
            case 1: {
                selectedFragment = new WeeklyFragment();
                break;
            }
            case 2: {
                selectedFragment = new MonthlyFragment();
                break;
            }
            case 3: {
                selectedFragment = new YearlyFragment();
                break;
            }
            case 4: {
                selectedFragment = new CalendarFragment();
                break;
            }
            default: {
                selectedFragment = new DailyFragment();
                break;
            }
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, selectedFragment);
        transaction.commitAllowingStateLoss();
        initializePanel();
    }

    private void initializeRevealAnimation() {
        revealAnimation.setFab(addFinanceButton);
        revealAnimation.setFabAnimationEndListener(() -> {
            Intent intent = new Intent(context, AddFinanceActivity.class);
            startActivityForResult(intent, ADD_FINANCE_REQUEST_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FINANCE_REQUEST_CODE) {
            revealAnimation.contractFab();
        }
    }

    @OnClick(R.id.add_finance_button)
    public void addFinanceButtonClick() {
        revealAnimation.expandFab();
    }

    @OnClick(R.id.next_button)
    public void nextButtonClick(View view) {
        Optional.ofNullable(selectedFragment).ifPresent(selectedFragment ->
                changeDate(determineDateElementToChange(selectedFragment), true));
    }

    @OnClick(R.id.previous_button)
    public void previousButton(View view) {
        Optional.ofNullable(selectedFragment).ifPresent(selectedFragment ->
                changeDate(determineDateElementToChange(selectedFragment), false));
    }

    private void changeDate(int exactDateElement, boolean next) {
        selectedPanelDate.add(exactDateElement, next ? 1 : -1);
        dateChangeLabel.setText(determineLeftPanelLabel(selectedFragment, selectedPanelDate.getTime()));
        selectedFragment.setSelectedPanelDate(selectedPanelDate);
    }

    @Override
    public void setPanelAmount(String amount) {
        financeSum.setText(amount);
    }

    @Override
    public void showInstructionPopup() {
    }

    @Override
    public void onResume() {
        super.onResume();
        financePresenter.loadFinances();
    }

}
