package org.pl.android.drively.ui.chat.chatview.messages;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.pl.android.drively.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemMessageUserHolder extends RecyclerView.ViewHolder implements MessageHolder {
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