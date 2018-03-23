package org.pl.android.drively.ui.chat.friendsearch;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.pl.android.drively.R;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class FriendModelAdapter extends RecyclerView.Adapter<FriendModelAdapter.ViewHolder> {

    protected Context mContext;
    private List<FriendModel> mItems = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int mLayout;
    private SearchResultListener mSearchResultListener;
    private AdapterViewBinder<FriendModel> mViewBinder;
    private String mSearchTag;
    private boolean mHighlightPartsInCommon = true;
    private BaseSearchDialogCompat mSearchDialog;


    public FriendModelAdapter(Context context, @LayoutRes int layout, List<FriendModel> items) {
        this(context, layout, null, items);
    }

    public FriendModelAdapter(Context context, @LayoutRes int layout,
                              @Nullable AdapterViewBinder<FriendModel> viewBinder,
                              List<FriendModel> items) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mItems = items;
        this.mLayout = layout;
        this.mViewBinder = viewBinder;
    }

    public List<FriendModel> getItems() {
        return mItems;
    }

    public void setItems(List<FriendModel> objects) {
        this.mItems = objects;
        notifyDataSetChanged();
    }

    public FriendModel getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public FriendModelAdapter setViewBinder(AdapterViewBinder<FriendModel> viewBinder) {
        this.mViewBinder = viewBinder;
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mLayoutInflater.inflate(mLayout, parent, false);
        convertView.setTag(new ViewHolder(convertView));
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FriendModelAdapter.ViewHolder holder, int position) {
        initializeViews(getItem(position), holder, position);
    }

    private void initializeViews(final FriendModel object, final FriendModelAdapter.ViewHolder holder,
                                 final int position) {
        if (mViewBinder != null)
            mViewBinder.bind(holder, object, position);

        LinearLayout root = holder.getViewById(R.id.root);
        TextView nameText = holder.getViewById(R.id.name);
        TextView emailText = holder.getViewById(R.id.email);
        CircleImageView avatar = holder.getViewById(R.id.image);

        if (object.getAvatar().equals(Const.STR_DEFAULT_AVATAR))
            avatar.setImageResource(R.drawable.default_avatar);
        else
            avatar.setImageBitmap(object.getAvatarImage());

        nameText.setText(object.getName());
        emailText.setText(object.getEmail());

        if (mSearchResultListener != null)
            holder.getBaseView().setOnClickListener(view -> mSearchResultListener.onSelected(mSearchDialog, object, position));
    }


    public void setSearchResultListener(SearchResultListener searchResultListener) {
        this.mSearchResultListener = searchResultListener;
    }

    public FriendModelAdapter setSearchDialog(BaseSearchDialogCompat searchDialog) {
        mSearchDialog = searchDialog;
        return this;
    }

    public interface AdapterViewBinder<T> {
        void bind(ViewHolder holder, T item, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View mBaseView;

        public ViewHolder(View view) {
            super(view);
            mBaseView = view;
        }

        public View getBaseView() {
            return mBaseView;
        }

        public <T> T getViewById(@IdRes int id) {
            return (T) mBaseView.findViewById(id);
        }

    }
}