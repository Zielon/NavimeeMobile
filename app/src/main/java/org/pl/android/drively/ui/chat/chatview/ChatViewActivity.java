package org.pl.android.drively.ui.chat.chatview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewActivity extends BaseActivity implements View.OnClickListener,ChatViewMvpView {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Conversation conversation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;
    public String UID;

    @Inject
    ChatViewPresenter mChatViewPresenter;

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

        String base64AvataUser =  "default";// SharedPreferenceHelper.getInstance(this).getUserInfo().avatar;
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
            adapter = new ListMessageAdapter(this, conversation, bitmapAvataFriend, bitmapAvataUser, mChatViewPresenter.getId());
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
        if(item.getItemId() == android.R.id.home){
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
                newMessage.idSender =  mChatViewPresenter.getId();
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
    private Bitmap bitmapAvataUser;
    private String mUID;

    public ListMessageAdapter(Context context, Conversation conversation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser, String UID) {
        this.context = context;
        this.conversation = conversation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
        mUID = UID;
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
        if (holder instanceof ItemMessageFriendHolder) {

            Message message = conversation.getListMessageData().get(position);
            ItemMessageFriendHolder messageFriendHolder = ((ItemMessageFriendHolder) holder);

            messageFriendHolder.txtContent.setText(message.text);
            Bitmap currentAvatar = bitmapAvata.get(message.idSender);

            messageFriendHolder.messageDialog = new MaterialDialog.Builder(context).customView(R.layout.friend_message_details, true).build();
            View view = messageFriendHolder.messageDialog.getView();
            String time = new SimpleDateFormat("EEE, MMM d, 'at' HH:mm").format(message.timestamp).toUpperCase();

            ((TextView)view.findViewById(R.id.message_time)).setText(time);
            ((TextView)view.findViewById(R.id.name)).setText(message.nameSender);
            ((TextView)view.findViewById(R.id.email)).setText(message.emailSender);
            ((CircleImageView)view.findViewById(R.id.avatar)).setImageDrawable(context.getResources().getDrawable(R.drawable.default_avatar));

            if (currentAvatar != null) {
                messageFriendHolder.avatar.setImageBitmap(currentAvatar);
            } else {
                final String id = message.idSender;
                if(bitmapAvataDB.get(id) == null){
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avatar"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(Const.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                }else{
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
            ItemMessageUserHolder messageUserHolder = ((ItemMessageUserHolder) holder);
            Message message = conversation.getListMessageData().get(position);
            String time = new SimpleDateFormat("EEE 'AT' HH:mm").format(message.timestamp).toUpperCase();

            messageUserHolder.txtContent.setText(message.text);
            messageUserHolder.timeStamp.setText(time);

            if (bitmapAvataUser != null) {
                ((ItemMessageUserHolder) holder).avatar.setImageBitmap(bitmapAvataUser);
            }
        }
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

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public TextView timeStamp;
    public CircleImageView avatar;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        timeStamp = (TextView) itemView.findViewById(R.id.message_info_user);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar_img_user);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public TextView timeStamp;
    public CircleImageView avatar;
    public MaterialDialog messageDialog;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        timeStamp = (TextView) itemView.findViewById(R.id.message_info_user);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar_img_friend);

        avatar.setOnClickListener(click -> {
            if(messageDialog != null)
                messageDialog.show();
        });
    }
}
