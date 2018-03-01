package org.pl.android.drively.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.RoomMember;
import org.pl.android.drively.data.model.chat.Group;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.data.GroupDB;

import java.util.ArrayList;
import java.util.HashMap;

import java8.util.stream.StreamSupport;

public class ChatUtils {

    public static String getRoomId(String a, String b) {
        return a.compareTo(b) > 0 ? (b + a).hashCode() + "" : "" + (a + b).hashCode();
    }

    public static Intent getChatActivityIntent(Context context, String groupName) {
        ListFriend listFriend = FriendDB.getInstance(context).getListFriend();
        Intent intent = new Intent(context, ChatViewActivity.class);
        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, groupName);
        ArrayList<CharSequence> idFriend = new ArrayList<>();
        ChatViewActivity.bitmapAvataFriend = new HashMap<>();
        ArrayList<Group> listGroup = GroupDB.getInstance(context).getListGroups();
        Group group = StreamSupport.stream(listGroup)
                .filter(singleGroup -> singleGroup.getName().equals(groupName))
                .findFirst().get();
        for (RoomMember member : group.getMembers()) {
            String id = member.getMemberId();
            idFriend.add(id);
            String avatar = listFriend.getAvataById(id);
            if (!avatar.equals(Const.STR_DEFAULT_AVATAR) && listFriend.getById(id) != null && listFriend.getById(id).avatarBytes != null) {
                ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(listFriend.getById(id).avatarBytes, 0, listFriend.getById(id).avatarBytes.length));
            } else if (avatar.equals(Const.STR_DEFAULT_AVATAR)) {
                ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
            } else {
                ChatViewActivity.bitmapAvataFriend.put(id, null);
            }
        }

        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, group.id);
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, true);

        return intent;
    }

}
