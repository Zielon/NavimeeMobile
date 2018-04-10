package org.pl.android.drively.ui.finance.form.edit;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.CategoryRepository;
import org.pl.android.drively.contracts.repositories.ExpenseRepository;
import org.pl.android.drively.contracts.repositories.IncomeRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.Finance;
import org.pl.android.drively.ui.finance.form.BaseFinanceFormPresenter;
import org.pl.android.drively.util.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class EditFinancePresenter extends BaseFinanceFormPresenter<EditFinanceMvpView> {

    @Inject
    EditFinancePresenter(CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider,
                         IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
                         CategoryRepository categoryRepository, DataManager dataManager) {
        super(compositeDisposable, schedulerProvider, incomeRepository, expenseRepository, categoryRepository, dataManager);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.dispose();
    }

    public StorageReference getFinanceImageReference(Finance finance) {
        return FirebaseStorage.getInstance().getReference().child(expenseRepository.buildPathByFinance(finance.getId()));
    }

    public void deleteExpense(String id) {
        attachRemoveListener(expenseRepository.remove(id));
    }

    private void attachRemoveListener(Task<Void> removeTask) {
        removeTask.addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                getMvpView().hideProgressDialog();
                getMvpView().goBackToFinances();
            } else {
                getMvpView().showMessage(R.string.deleting_failure);
            }
        });
    }

    public void deleteIncome(String id) {
        attachRemoveListener(incomeRepository.remove(id));
    }
}
