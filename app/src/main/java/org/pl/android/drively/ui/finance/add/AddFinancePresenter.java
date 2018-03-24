package org.pl.android.drively.ui.finance.add;


import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.FinanceRepository;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.data.model.chip.CategoryChip;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class AddFinancePresenter extends BasePresenter<AddFinanceMvpView> {

    private final FinanceRepository financeRepository;

    private CompositeDisposable compositeDisposable;

    private SchedulerProvider schedulerProvider;

    private CategoryChip lastInputChip;

    private List<CategoryChip> categoryChips;

    @Inject
    AddFinancePresenter(FinanceRepository financeRepository, CompositeDisposable compositeDisposable,
                        SchedulerProvider schedulerProvider) {
        this.financeRepository = financeRepository;
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
    }

    void saveFinance(Finance finance) {
        financeRepository.save(finance).addOnCompleteListener(documentReference -> {
            getMvpView().hideProgressDialog();
            if (documentReference.isSuccessful()) {
                getMvpView().finishActivity();
            } else {
                getMvpView().showMessage(R.string.finance_failed_to_add);
            }
        });
    }

    void loadChips() {
        compositeDisposable.add(Observable.fromIterable(Const.FINANCE_CATEGORIES)
                .map(CategoryChip::new)
                .toList()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(categoryChips -> {
                    getMvpView().setChips(categoryChips);
                    AddFinancePresenter.this.categoryChips = categoryChips;
                })
        );
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.dispose();
    }
}
