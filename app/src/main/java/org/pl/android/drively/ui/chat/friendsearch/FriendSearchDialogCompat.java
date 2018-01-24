package org.pl.android.drively.ui.chat.friendsearch;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pl.android.drively.R;

import java.util.ArrayList;

import ir.mirrajabi.searchdialog.core.BaseFilter;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.OnPerformFilterListener;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class FriendSearchDialogCompat extends BaseSearchDialogCompat<FriendModel> {

    private String mTitle;
    private String mSearchHint;
    private SearchResultListener<FriendModel> mSearchResultListener;
    private ProgressBar mProgressBar;
    private EditText mSearchBox;
    private Handler mHandler;
    private RecyclerView mRecyclerView;

    public FriendSearchDialogCompat(Context context, String title, String searchHint,
                                    @Nullable Filter filter, ArrayList<FriendModel> items,
                                    SearchResultListener<FriendModel> searchResultListener) {
        super(context, items, filter, null, null);
        init(title, searchHint, searchResultListener);
    }

    private void init(String title, String searchHint,
                      SearchResultListener<FriendModel> searchResultListener) {
        mTitle = title;
        mSearchHint = searchHint;
        mSearchResultListener = searchResultListener;
    }

    @Override
    protected void getView(View view) {
        setContentView(view);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(true);
        mProgressBar = (ProgressBar) view.findViewById(ir.mirrajabi.searchdialog.R.id.progress);
        mSearchBox = (EditText) view.findViewById(getSearchBoxId());
        mRecyclerView = (RecyclerView) view.findViewById(getRecyclerViewId());

        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        TextView txtTitle = (TextView) view.findViewById(ir.mirrajabi.searchdialog.R.id.txt_title);
        final EditText searchBox = (EditText) view.findViewById(getSearchBoxId());

        txtTitle.setText(mTitle);
        searchBox.setHint(mSearchHint);
        view.findViewById(ir.mirrajabi.searchdialog.R.id.dummy_background).setOnClickListener(view1 -> dismiss());

        final FriendModelAdapter adapter = new FriendModelAdapter(getContext(), R.layout.friends_search_adapter_item, getItems());

        adapter.setSearchResultListener(mSearchResultListener);
        adapter.setSearchDialog(this);
        setFilterResultListener(items -> ((FriendModelAdapter) getAdapter()).setItems(items));
        setFilterAutomatically(true);
        setAdapter(adapter);

        mHandler = new Handler();
        mSearchBox.requestFocus();

        ((BaseFilter<FriendModel>) getFilter()).setOnPerformFilterListener(new OnPerformFilterListener() {
            @Override
            public void doBeforeFiltering() {
                setLoading(true);
            }

            @Override
            public void doAfterFiltering() {
                setLoading(false);
            }
        });
    }

    public void setLoading(final boolean isLoading) {
        mHandler.post(() -> {
            if (mProgressBar != null)
                mRecyclerView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
            if (mRecyclerView != null)
                mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    public FriendSearchDialogCompat setTitle(String title) {
        mTitle = title;
        return this;
    }

    public FriendSearchDialogCompat setSearchHint(String searchHint) {
        mSearchHint = searchHint;
        return this;
    }

    public FriendSearchDialogCompat setSearchResultListener(
            SearchResultListener<FriendModel> searchResultListener) {
        mSearchResultListener = searchResultListener;
        return this;
    }

    @LayoutRes
    @Override
    protected int getLayoutResId() {
        return R.layout.friends_search_dialog_compat;
    }

    @IdRes
    @Override
    protected int getSearchBoxId() {
        return ir.mirrajabi.searchdialog.R.id.txt_search;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return ir.mirrajabi.searchdialog.R.id.rv_items;
    }
}
