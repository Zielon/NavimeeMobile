package org.pl.android.drively.ui.chat.chatview.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Conversation;
import org.pl.android.drively.data.model.chat.GroupMessage;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewPresenter;
import org.pl.android.drively.util.ChatUtils;
import org.pl.android.drively.util.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Getter;

import static org.pl.android.drively.util.Const.ADMIN;

public class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    @Getter
    private Conversation conversation;
    private Bitmap bitmapAvatarUser;
    private ChatViewPresenter chatViewPresenter;

    public ListMessageAdapter(Context context,
                              Conversation conversation,
                              Bitmap bitmapAvatarUser, ChatViewPresenter chatViewPresenter) {
        this.context = context;
        this.conversation = conversation;
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

    public void addToStart(List<Message> messages) {
        conversation.getListMessageData().addAll(0, messages);
    }

    public void addToEnd(List<Message> messages) {
        conversation.getListMessageData().addAll(messages);
    }

    public void groupMessages(int position, MessageHolder holder) {
        List<Message> messages = conversation.getListMessageData();
        resetHolder(holder);

        if (messages.size() <= 0) return;

        Message current = messages.get(position);
        Message next = messages.size() > 1 && position > 0 ? messages.get(position - 1) : null;
        Message previous = position + 1 < messages.size() ? messages.get(position + 1) : null;

        // The screen oriented position
        boolean BETWEEN = previous != null && next != null && previous.idSender.equals(current.idSender) && next.idSender.equals(current.idSender);
        boolean BOTTOM = previous != null && next != null && !next.idSender.equals(current.idSender) && previous.idSender.equals(current.idSender);
        boolean TOP = next != null && previous != null && !previous.idSender.equals(current.idSender) && next.idSender.equals(current.idSender);
        boolean LAST = previous != null && position == 0 && previous.idSender.equals(current.idSender);
        boolean FIRST = next != null && position == messages.size() - 1 && next.idSender.equals(current.idSender);

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
            Bitmap currentAvatar = chatViewPresenter.getFriendAvatar(message.idSender);

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