package org.pl.android.drively.ui.finance;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.common.SheetLayout;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.finance.add.AddFinanceActivity;
import org.pl.android.drively.ui.finance.list.FinanceAdapter;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

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

    @BindView(R.id.finance_recycler_view)
    RecyclerView financeRecyclerView;

    FinanceAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && actionBar.getCustomView() != null) {
            TextView text = (TextView) actionBar.getCustomView().findViewById(R.id.app_bar_text);
            text.setText(getResources().getString(R.string.finance));
        }
        context = (MainActivity) this.getActivity();

    }

    private void initializeView() {
        adapter = new FinanceAdapter(new ArrayList<>());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context.getApplicationContext());
        financeRecyclerView.setLayoutManager(mLayoutManager);
        financeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        financeRecyclerView.setAdapter(adapter);
    }

    @Override
    public void showNoDataLabel() {
        noDataLabel.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateFinances(List<? extends Finance> finances) {
        adapter.setFinances(finances);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.finance_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        financePresenter.attachView(this);
        initializeView();
        financePresenter.loadFinances();
        initializeRevealAnimation();
        return fragmentView;
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

    @Override
    public void showInstructionPopup() {
    }

    @Override
    public void onResume() {
        super.onResume();
        financePresenter.loadFinances();
    }

}
