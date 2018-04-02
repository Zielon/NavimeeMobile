package org.pl.android.drively.ui.chat.chatview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.pl.android.drively.R;
import org.pl.android.drively.common.EndlessRecyclerViewScrollListener;
import org.pl.android.drively.data.model.chat.Conversation;
import org.pl.android.drively.data.model.chat.GroupMessage;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.chatview.messages.ListMessageAdapter;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatViewActivity extends BaseActivity implements View.OnClickListener, ChatViewMvpView {

    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    public static Bitmap bitmapAvatarUser;
    public static String ACTIVE_ROOM = "";

    @Inject
    ChatViewPresenter mChatViewPresenter;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerChat)
    RecyclerView recyclerChat;
    private String roomId;
    private String roomName;
    private ArrayList<CharSequence> idFriend;
    private Conversation conversation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    private Boolean isGroupChat;
    private ListMessageAdapter adapter;
    private boolean allMessagesLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        mChatViewPresenter.attachView(this);
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(Const.INTENT_KEY_CHAT_ROOM_ID);
        roomName = intentData.getStringExtra(Const.INTENT_KEY_CHAT_ROOM_NAME);
        isGroupChat = intentData.getBooleanExtra(Const.INTENT_KEY_IS_GROUP_CHAT, false);
        String nameFriend = intentData.getStringExtra(Const.INTENT_KEY_CHAT_FRIEND);
        conversation = new Conversation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        swipeRefreshLayout.setRefreshing(true);
        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);

        if (!isGroupChat && nameFriend != null)
            getSupportActionBar().setTitle(nameFriend);
        else
            getSupportActionBar().setTitle(roomName);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerChat.setLayoutManager(linearLayoutManager);

        adapter = new ListMessageAdapter(this,
                conversation,
                bitmapAvatarUser,
                mChatViewPresenter);

        recyclerChat.setAdapter(adapter);

        recyclerChat.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!allMessagesLoaded) {
                    swipeRefreshLayout.setRefreshing(true);
                    mChatViewPresenter.getSingleBatch();
                }
            }
        });

        mChatViewPresenter.setIntentDataAndBuildBaseQuery(isGroupChat, roomId);
        mChatViewPresenter.getSingleBatch();
        mChatViewPresenter.setNewMessagesListenerTimestamp();
    }

    @Override
    public void addNewBatch(List<Message> messages) {
        if (!messages.isEmpty()) {
            int currentSize = adapter.getItemCount();
            adapter.addToEnd(messages);
            adapter.notifyItemRangeInserted(currentSize, adapter.getConversation().getListMessageData().size() - 1);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void addMessagesAtTheBeginning(List<Message> messages) {
        adapter.addToStart(messages);
        adapter.notifyDataSetChanged();
        linearLayoutManager.scrollToPosition(0);
    }

    public void setAllMessagesLoaded(boolean allMessagesLoaded) {
        this.allMessagesLoaded = allMessagesLoaded;
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Conversation getConversation() {
        return adapter.getConversation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent result = new Intent();
            if (idFriend != null && idFriend.size() > 0)
                result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatViewPresenter.detachView();
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        if (idFriend != null && idFriend.size() > 0)
            result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        ACTIVE_ROOM = roomId;
    }

    @Override
    public void onStop() {
        super.onStop();
        ACTIVE_ROOM = "";
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                Message newMessage = isGroupChat ? new GroupMessage() : new PrivateMessage(idFriend.get(0).toString());

                newMessage.text = content;
                newMessage.idSender = mChatViewPresenter.getId();
                newMessage.idRoom = roomId;
                newMessage.nameSender = mChatViewPresenter.getUserInfo().getName();
                newMessage.emailSender = mChatViewPresenter.getUserInfo().getEmail();
                newMessage.timestamp = System.currentTimeMillis();

                mChatViewPresenter.addMessage(newMessage);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

}


