package org.pl.android.drively.ui.finance.pages;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pl.android.drively.R;
import org.pl.android.drively.util.Const;

public class FinanceFragmentPagerAdapter extends PagerAdapter {

    private LayoutInflater inflater;

    public FinanceFragmentPagerAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;
        switch (position) {
            case 0:
            case 1:
            case 2:
            default: {
                view = inflater.inflate(R.layout.finances_recycler_view, null, false);
                break;
            }
        }
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return Const.FINANCE_VIEW_PAGER_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final View container, final int position, final Object object) {
        ((ViewPager) container).removeView((View) object);
    }
}