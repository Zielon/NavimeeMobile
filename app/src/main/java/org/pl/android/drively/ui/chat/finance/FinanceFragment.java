package org.pl.android.drively.ui.chat.finance;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.base.tab.BaseTabFragment;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.util.Const;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FinanceFragment extends BaseTabFragment implements FinanceMvpView {

    @Inject
    FinancePresenter financePresenter;

    private MaterialDialog popup;

    private MainActivity context;

    public static FinanceFragment newInstance() {
        return new FinanceFragment();
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.finance_fragment, container, false);
        ButterKnife.bind(this, fragmentView);
        financePresenter.attachView(this);
        return fragmentView;
    }

    @OnClick(R.id.popup_finance_contact_us_button)
    public void onContanctUsClick(View view) {
        changeTabToChat();
        financePresenter.getDrivelyGroup();
    }


    @Override
    public void showInstructionPopup() {
    }

    @Override
    public void goToDrivelyChat(Room room) {
        Intent intent = new Intent(context, ChatViewActivity.class);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, room.getId());
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, true);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_NAME, room.getName());
        context.startActivity(intent);
        popup.dismiss();
    }

    private void changeTabToChat() {
        context.changeTabByResId(R.id.tab_chat);
    }

}
