package org.pl.android.drively.ui.finance.add;

import org.pl.android.drively.data.model.chip.CategoryChip;
import org.pl.android.drively.ui.base.progress.BaseProgressMvp;

import java.util.List;

public interface AddFinanceMvpView extends BaseProgressMvp {

    void setChips(List<CategoryChip> categoryChips);

    void finishActivity();

}
