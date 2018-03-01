package org.pl.android.drively.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.data.model.chat.RoomMember;
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
        ChatViewActivity.bitmapAvatarFriends = new HashMap<>();
        ArrayList<Room> listGroup = GroupDB.getInstance(context).getListGroups();
        Room group = StreamSupport.stream(listGroup)
                .filter(singleGroup -> singleGroup.getName().equals(groupName))
                .findFirst().get();
        for (RoomMember member : group.getMembers()) {
            String id = member.getMemberId();
            idFriend.add(id);
            String avatar = listFriend.getAvataById(id);
            if (!avatar.equals(Const.STR_DEFAULT_AVATAR) && listFriend.getById(id) != null && listFriend.getById(id).avatarBytes != null) {
                ChatViewActivity.bitmapAvatarFriends.put(id, BitmapFactory.decodeByteArray(listFriend.getById(id).avatarBytes, 0, listFriend.getById(id).avatarBytes.length));
            } else if (avatar.equals(Const.STR_DEFAULT_AVATAR)) {
                ChatViewActivity.bitmapAvatarFriends.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
            } else {
                ChatViewActivity.bitmapAvatarFriends.put(id, null);
            }
        }

        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, group.getId());
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, true);

        return intent;
    }

}
