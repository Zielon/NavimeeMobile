package org.pl.android.drively.ui.chat.chatview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Conversation;
import org.pl.android.drively.data.model.chat.GroupMessage;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.util.ChatUtils;
import org.pl.android.drively.util.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import timber.log.Timber;

import static org.pl.android.drively.util.Const.ADMIN;

interface MessageHolder {
    TextView getTextContent();

    TextView getTimestamp();

    CircleImageView getAvatar();

    RelativeLayout getLayout();

    RecyclerView.ViewHolder getViewHolder();
}

public class ChatViewActivity extends BaseActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, ChatViewMvpView,
        MessagesListAdapter.SelectionListener, MessagesListAdapter.OnLoadMoreListener {

    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    public static HashMap<String, Bitmap> bitmapAvatarFriends = new HashMap<>();
    public static Bitmap bitmapAvatarUser;
    public static String ACTIVE_ROOM = "";
    public String UID;

    @Inject
    ChatViewPresenter mChatViewPresenter;
    private ListMessageAdapter adapter;
    private String roomId;
    private String roomName;
    private ArrayList<CharSequence> idFriend;
    private Conversation conversation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    private Boolean isGroupChat;

    @BindView(R.id.messagesList)
    MessagesList messagesList;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private int refreshState;

    private boolean wasScrooledToEnd = false;

    private boolean loadingInProgress = true;

    private boolean hasLoadedAllItems;

    private int lastSize;

    protected MessagesListAdapter<Message> messagesAdapter;

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
        initAdapter();
        conversation = new Conversation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);

        if (!isGroupChat && nameFriend != null)
            getSupportActionBar().setTitle(nameFriend);
        else
            getSupportActionBar().setTitle(roomName);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        adapter = new ListMessageAdapter(this,
                conversation,
                bitmapAvatarFriends,
                bitmapAvatarUser,
                mChatViewPresenter);

        mChatViewPresenter.setGroupChat(isGroupChat);
        mChatViewPresenter.setRoomId(roomId);
        mChatViewPresenter.getSingleBatch();
        mChatViewPresenter.updateNewMessagesListenerTimestamp(getFirstMessageTimestamp());

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void roomChangesListerSet(List<Message> messages) {
        if (!messages.isEmpty()) {
            messages = filterAlreadyAttachedMessages(messages);
            try {
                messagesAdapter.addToEnd(messages, true);
            } catch (IndexOutOfBoundsException e) {
                Timber.d(e);
            }
            conversation.getListMessageData().addAll(messages);
            mChatViewPresenter.updateNewMessagesListenerTimestamp(getFirstMessageTimestamp());
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void addMessagesAtTheBeginning(List<Message> messages) {
        messages = filterAlreadyAttachedMessages(messages);
        StreamSupport.stream(messages)
                .forEach(message -> {
                    messagesAdapter.addToStart(message, false);
                    conversation.getListMessageData().add(message);
                });
        mChatViewPresenter.updateNewMessagesListenerTimestamp(getFirstMessageTimestamp());
    }

    private List<Message> filterAlreadyAttachedMessages(List<Message> messageList) {
        if (!conversation.getListMessageData().isEmpty()) {
            return StreamSupport.stream(messageList)
                    .filter(message -> !conversation.getListMessageData().contains(message) && message != null)
                    .collect(Collectors.toList());
        } else {
            return messageList;
        }
    }

    private Long getFirstMessageTimestamp() {
        if (!conversation.getListMessageData().isEmpty()) {
            return StreamSupport.stream(conversation.getListMessageData())
                    .sorted((message1, message2) ->
                            Integer.valueOf((int) message2.timestamp).compareTo((int) message1.timestamp))
                    .collect(Collectors.toList())
                    .get(0)
                    .getTimestamp() + 1;
        } else {
            return 0L;
        }
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

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(mChatViewPresenter.getId(), (imageView, userId) -> {
                mChatViewPresenter.getAvatarPathFromFirebaseByUserEmail(userId, (url, isDefault) ->
                        Glide.with(ChatViewActivity.this).load(isDefault ? R.drawable.default_avatar : url).into(imageView));
        }
        );
        messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                (view, message) -> Toast.makeText(ChatViewActivity.this, "view clicked co", Toast.LENGTH_SHORT));
        this.messagesList.setAdapter(messagesAdapter);
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

                mChatViewPresenter.addMessage(roomId, newMessage);
//                linearLayoutManager.scrollToPosition(conversation.getListMessageData().size() - 1);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        swipeRefreshLayout.setRefreshing(true);
        mChatViewPresenter.getSingleBatch();
    }

    @Override
    public void onSelectionChanged(int count) {

    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Conversation conversation;
    private HashMap<String, Bitmap> bitmapAvatars;
    private Bitmap bitmapAvatarUser;
    private ChatViewPresenter chatViewPresenter;
    private RecyclerViewPositionHelper positionHelper;

    public ListMessageAdapter(Context context,
                              Conversation conversation,
                              HashMap<String, Bitmap> bitmapAvatars,
                              Bitmap bitmapAvatarUser, ChatViewPresenter chatViewPresenter) {
        this.context = context;
        this.conversation = conversation;
        this.bitmapAvatars = bitmapAvatars;
        this.bitmapAvatarUser = bitmapAvatarUser;
        this.chatViewPresenter = chatViewPresenter;
    }

    public static void setMargins(RelativeLayout layout, Integer l, Integer t, Integer r, Integer b) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        marginLayoutParams.setMargins(
                l != null ? l : marginLayoutParams.leftMargin,
                t != null ? t : marginLayoutParams.topMargin,
                r != null ? r : marginLayoutParams.rightMargin,
                b != null ? b : marginLayoutParams.bottomMargin);
        layout.setLayoutParams(marginLayoutParams);
    }

    public static void resetHolder(MessageHolder holder) {
        if (holder == null) return;
        holder.getTimestamp().setVisibility(View.VISIBLE);
        holder.getAvatar().setVisibility(View.VISIBLE);
    }

    public void groupMessages(int position, MessageHolder holder) {
        List<Message> messages = conversation.getListMessageData();
        resetHolder(holder);

        if (messages.size() <= 0) return;

        // Starts from the bottom.
        Message current = messages.get(position);
        Message next = messages.size() > 1 && position > 0 ? messages.get(position - 1) : null;
        Message previous = position + 1 < messages.size() ? messages.get(position + 1) : null;

        // The screen oriented position
        boolean BETWEEN = previous != null && next != null && previous.idSender.equals(current.idSender) && next.idSender.equals(current.idSender);
        boolean TOP = previous != null && next != null && !next.idSender.equals(current.idSender) && previous.idSender.equals(current.idSender);
        boolean BOTTOM = next != null && previous != null && !previous.idSender.equals(current.idSender) && next.idSender.equals(current.idSender);
        boolean LAST = next != null && position == messages.size() - 1 && next.idSender.equals(current.idSender);
        boolean FIRST = previous != null && position == 0 && previous.idSender.equals(current.idSender);

        if (BETWEEN) {
            holder.getAvatar().setVisibility(View.INVISIBLE);
            holder.getTimestamp().setVisibility(View.GONE);
            setMargins(holder.getLayout(), null, 0, null, 0);
        }

        if (TOP || FIRST) {
            holder.getAvatar().setVisibility(View.INVISIBLE);
            setMargins(holder.getLayout(), null, 0, null, 0);
        }
        if (BOTTOM || LAST) {
            holder.getTimestamp().setVisibility(View.GONE);
            setMargins(holder.getLayout(), null, 0, null, 10);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatViewActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatViewActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Message message = conversation.getListMessageData().get(position);

        if (holder instanceof ItemMessageFriendHolder) {
            ItemMessageFriendHolder messageHolder = ((ItemMessageFriendHolder) holder);
            messageHolder.txtContent.setText(message.text);
            Bitmap currentAvatar = bitmapAvatars.get(message.idSender);

            messageHolder.messageDialog = new MaterialDialog.Builder(context)
                    .backgroundColorRes(R.color.primary)
                    .customView(R.layout.friend_message_details, true)
                    .build();

            View view = messageHolder.messageDialog.getView();
            String time = new SimpleDateFormat("EEE, MMM d, 'at' HH:mm").format(message.timestamp).toUpperCase();

            ((TextView) view.findViewById(R.id.message_time)).setText(time);
            ((TextView) view.findViewById(R.id.name)).setText(message.nameSender);

            if (message instanceof GroupMessage)
                view.findViewById(R.id.sendMessage).setOnClickListener((onClick) -> {
                    Intent intent = new Intent(context, ChatViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, message.nameSender);
                    ArrayList<CharSequence> idFriend = new ArrayList<>();
                    idFriend.add(message.idSender);
                    intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
                    intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, ChatUtils.getRoomId(message.idSender, chatViewPresenter.getId()));
                    chatViewPresenter.addFriend(message.idSender);
                    context.startActivity(intent);
                });

            if (message.deleted || message.idSender.equals(ADMIN) || message instanceof PrivateMessage)
                view.findViewById(R.id.sendMessage).setVisibility(View.GONE);

            messageHolder.timeStamp.setText(new SimpleDateFormat("EEE 'AT' HH:mm").format(message.timestamp).toUpperCase());

            if (currentAvatar != null) {
                messageHolder.avatar.setImageBitmap(currentAvatar);
                ((CircleImageView) view.findViewById(R.id.avatar)).setImageBitmap(currentAvatar);
            } else {
                messageHolder.avatar.setImageResource(R.drawable.default_avatar);
                // Details images
                if (message.idSender.equals(ADMIN)) {
                    ((CircleImageView) view.findViewById(R.id.avatar)).setImageResource(R.drawable.drively);
                    messageHolder.avatar.setImageResource(R.drawable.drively);
                } else
                    ((CircleImageView) view.findViewById(R.id.avatar)).setImageResource(R.drawable.default_avatar);
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            ItemMessageUserHolder messageHolder = ((ItemMessageUserHolder) holder);
            String time = new SimpleDateFormat("EEE 'AT' HH:mm").format(message.timestamp).toUpperCase();
            messageHolder.txtContent.setText(message.text);
            messageHolder.timeStamp.setText(time);
            if (bitmapAvatarUser != null)
                messageHolder.avatar.setImageBitmap(bitmapAvatarUser);
            else
                messageHolder.avatar.setImageResource(R.drawable.default_avatar);
        }

        groupMessages(position, (MessageHolder) holder);
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).idSender.equals(chatViewPresenter.getId()) ? ChatViewActivity.VIEW_TYPE_USER_MESSAGE : ChatViewActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder implements MessageHolder {
    public TextView txtContent;
    public TextView timeStamp;
    public CircleImageView avatar;
    public RelativeLayout layout;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        timeStamp = (TextView) itemView.findViewById(R.id.message_info_user);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar_img_user);
        layout = (RelativeLayout) itemView.findViewById(R.id.user_message_layout);
    }

    @Override
    public TextView getTextContent() {
        return txtContent;
    }

    @Override
    public TextView getTimestamp() {
        return timeStamp;
    }

    @Override
    public CircleImageView getAvatar() {
        return avatar;
    }

    @Override
    public RelativeLayout getLayout() {
        return layout;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder() {
        return this;
    }

}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder implements MessageHolder {
    public TextView txtContent;
    public TextView timeStamp;
    public CircleImageView avatar;
    public MaterialDialog messageDialog;
    public RelativeLayout layout;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        timeStamp = (TextView) itemView.findViewById(R.id.message_info_friend);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar_img_friend);
        layout = (RelativeLayout) itemView.findViewById(R.id.friend_message_layout);

        avatar.setOnClickListener(click -> {
            if (messageDialog != null)
                messageDialog.show();
        });

        txtContent.setOnClickListener(click -> {
            if (messageDialog != null)
                messageDialog.show();
        });
    }

    @Override
    public TextView getTextContent() {
        return txtContent;
    }

    @Override
    public TextView getTimestamp() {
        return timeStamp;
    }

    @Override
    public CircleImageView getAvatar() {
        return avatar;
    }

    @Override
    public RelativeLayout getLayout() {
        return layout;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder() {
        return this;
    }
}
