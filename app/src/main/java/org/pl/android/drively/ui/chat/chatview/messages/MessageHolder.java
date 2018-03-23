package org.pl.android.drively.ui.chat.chatview.messages;

import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

interface MessageHolder {
    TextView getTimestamp();

    CircleImageView getAvatar();

    RelativeLayout getLayout();
}
