package org.pl.android.drively.ui.chat.chatview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Conversation;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.util.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewActivity extends BaseActivity implements View.OnClickListener, ChatViewMvpView {
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;
    public String UID;
    @Inject
    ChatViewPresenter mChatViewPresenter;
    private RecyclerView recyclerChat;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Conversation conversation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_chat);
        mChatViewPresenter.attachView(this);
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(Const.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(Const.INTENT_KEY_CHAT_FRIEND);

        conversation = new Conversation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        String base64AvataUser = "default";// SharedPreferenceHelper.getInstance(this).getUserInfo().avatar;
        if (!base64AvataUser.equals(Const.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
        if (idFriend != null && nameFriend != null) {
            getSupportActionBar().setTitle(nameFriend);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);

            adapter = new ListMessageAdapter(this,
                            conversation,
                            new RecyclerViewPositionHelper(recyclerChat),
                            bitmapAvataFriend,
                            bitmapAvataUser,
                            mChatViewPresenter.getId());

            mChatViewPresenter.setMessageListener(roomId);
            recyclerChat.setAdapter(adapter);
        }
    }


    @Override
    public void roomChangesListerSet(List<Message> message) {
        conversation.getListMessageData().clear();
        conversation.getListMessageData().addAll(message);
        adapter.notifyDataSetChanged();
        linearLayoutManager.scrollToPosition(conversation.getListMessageData().size() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent result = new Intent();
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
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                Message newMessage = new Message();
                newMessage.text = content;
                newMessage.idSender = mChatViewPresenter.getId();
                newMessage.idReceiver = roomId;
                newMessage.nameSender = mChatViewPresenter.getUserInfo().getName();
                newMessage.emailSender = mChatViewPresenter.getUserInfo().getEmail();
                newMessage.timestamp = System.currentTimeMillis();
                mChatViewPresenter.addMessage(roomId, newMessage);
            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Conversation conversation;
    private HashMap<String, Bitmap> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private Map<Integer, MessageHolder> messagesHolders;
    private Bitmap bitmapAvataUser;
    private String mUID;
    private RecyclerViewPositionHelper positionHelper;

    public ListMessageAdapter(Context context, Conversation conversation, RecyclerViewPositionHelper positionHelper, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser, String UID) {
        this.context = context;
        this.conversation = conversation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        this.bitmapAvataDB = new HashMap<>();
        this.mUID = UID;
        this.messagesHolders = new HashMap<>();
        this.positionHelper = positionHelper;
    }

    public void groupMessages(){
        List<List<Integer>> messagesGroups = new ArrayList<>();
        List<Message> messages = conversation.getListMessageData();
        int first = positionHelper.findFirstVisibleItemPosition();
        int last = positionHelper.findLastVisibleItemPosition();

        if(messages.size() < 2 || first < 0 || last < 0) return;

        for (int i = 0; i < messages.size(); i++) {
            resetHolder(messagesHolders.get(i));
            Message message = messages.get(i);
            Message nextMessage = messages.get(i + 1);

            List<Integer> group = new ArrayList<>();
            group.add(i);

            while(message.idSender.equals(nextMessage.idSender)){
                group.add(i + 1);
                message = messages.get(i++);
                if(i + 1 > messages.size() - 1) break;
                nextMessage = messages.get(i + 1);
            }

            if(group.size() > 1)
                messagesGroups.add(group);
        }

        for(List<Integer> group : messagesGroups) {

            MessageHolder firstHolder = messagesHolders.get(group.get(0));
            MessageHolder lastHolder = messagesHolders.get(group.get(group.size() - 1));

            for(Integer position : group) {
                MessageHolder holder = messagesHolders.get(position);
                if(holder == null) break;
                holder.getAvatar().setVisibility(View.INVISIBLE);
                holder.getTimestamp().setVisibility(View.INVISIBLE);
                setMargins(holder.getLayout(), 0,0,0,0);
            }

            if(firstHolder != null){
                firstHolder.getTimestamp().setVisibility(View.VISIBLE);
            }

            if(lastHolder != null){
                lastHolder.getAvatar().setVisibility(View.VISIBLE);
            }
        }
    }

    public static void setMargins (RelativeLayout layout, int l, int t, int r, int b) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)(layout.getLayoutParams());
        params.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(params);
    }

    public static void resetHolder(MessageHolder holder){
        if(holder == null) return;
        holder.getTimestamp().setVisibility(View.VISIBLE);
        holder.getAvatar().setVisibility(View.VISIBLE);
        setMargins(holder.getLayout(), 0,10,0,10);
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
            Bitmap currentAvatar = bitmapAvata.get(message.idSender);

            messageHolder.messageDialog = new MaterialDialog.Builder(context).customView(R.layout.friend_message_details, true).build();
            View view = messageHolder.messageDialog.getView();
            String time = new SimpleDateFormat("EEE, MMM d, 'at' HH:mm").format(message.timestamp).toUpperCase();

            ((TextView) view.findViewById(R.id.message_time)).setText(time);
            ((TextView) view.findViewById(R.id.name)).setText(message.nameSender);
            ((TextView) view.findViewById(R.id.email)).setText(message.emailSender);
            ((CircleImageView) view.findViewById(R.id.avatar)).setImageDrawable(context.getResources().getDrawable(R.drawable.default_avatar));

            if (currentAvatar != null) {
                messageHolder.avatar.setImageBitmap(currentAvatar);
            } else {
                final String id = message.idSender;
                if (bitmapAvataDB.get(id) == null) {
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avatar"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if (!avataStr.equals(Const.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                } else {
                                    ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            ItemMessageUserHolder messageHolder = ((ItemMessageUserHolder) holder);
            String time = new SimpleDateFormat("EEE 'AT' HH:mm").format(message.timestamp).toUpperCase();
            messageHolder.txtContent.setText(message.text);
            messageHolder.timeStamp.setText(time);
        }

        if(!messagesHolders.containsKey(position))
            messagesHolders.put(position, (MessageHolder)holder);
        groupMessages();
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).idSender.equals(mUID) ? ChatViewActivity.VIEW_TYPE_USER_MESSAGE : ChatViewActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }
}

interface MessageHolder{
     TextView getTextContent();
     TextView getTimestamp();
     CircleImageView getAvatar();
     RelativeLayout getLayout();
     RecyclerView.ViewHolder getViewHolder();
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
