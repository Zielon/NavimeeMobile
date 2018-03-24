package org.pl.android.drively.ui.chat.chatview.messages;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.pl.android.drively.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemMessageFriendHolder extends RecyclerView.ViewHolder implements MessageHolder {
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

}
