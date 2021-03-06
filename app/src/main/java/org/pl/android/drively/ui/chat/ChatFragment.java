package org.pl.android.drively.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.chat.friends.FriendsFragment;
import org.pl.android.drively.ui.chat.group.GroupFragment;
import org.pl.android.drively.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatFragment extends BaseTabFragment implements ChatMvpView {


    public static String STR_FRIEND_FRAGMENT = "FRIEND";
    public static String STR_GROUP_FRAGMENT = "GROUP";
    @Inject
    ChatPresenter mChatPresenter;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    private MaterialDialog popup;

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && actionBar.getCustomView() != null) {
            TextView text = (TextView) actionBar.getCustomView().findViewById(R.id.app_bar_text);
            text.setText(getResources().getString(R.string.chat));
            actionBar.show();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.chat_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        mChatPresenter.attachView(this);
        initTab();
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChatPresenter.detachView();
    }

    @Override
    public void showError() {

    }

    private void initTab() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(this.getFragmentManager());
        adapter.addFrag(new GroupFragment(), STR_GROUP_FRAGMENT);
        adapter.addFrag(new FriendsFragment(), STR_FRIEND_FRAGMENT);
        viewPager.setAdapter(adapter);
        //  viewPager.setOffscreenPageLimit(2);
    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_group_black_24dp,
                R.drawable.ic_person_black_24dp
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    @Override
    public void showInstructionPopup() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_popup_instruction, null);
        preparePopupLayout(view);
        popup = new MaterialDialog.Builder(getActivity())
                .customView(view, false)
                .backgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent))
                .show();
    }

    private void preparePopupLayout(View rootView) {
        rootView.findViewById(R.id.popup_chat_understood_button).setOnClickListener(view -> popup.dismiss());
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }


        // This is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }
    }
}
