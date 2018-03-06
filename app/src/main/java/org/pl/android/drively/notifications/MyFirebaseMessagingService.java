package org.pl.android.drively.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.joda.time.DateTime;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.notifications.EventNotificationFCM;
import org.pl.android.drively.data.model.notifications.FeedbackNotificationFCM;
import org.pl.android.drively.data.model.notifications.MessageNotificationFCM;
import org.pl.android.drively.data.model.notifications.MessageNotificationGroupFCM;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;

import javax.inject.Inject;

import static org.pl.android.drively.util.BitmapUtils.getCircular;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Inject
    DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Const.NotificationsType type = Const.NotificationsType.valueOf(remoteMessage.getData().get("type"));
        final ObjectMapper mapper = new ObjectMapper();
        switch (type) {
            case FEEDBACK:
                final FeedbackNotificationFCM feedbackNotification = mapper.convertValue(remoteMessage.getData(), FeedbackNotificationFCM.class);
                dataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK, true);
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_NAME, feedbackNotification.getLocationName());
                dataManager.getPreferencesHelper().setValue(Const.NAME, feedbackNotification.getName());
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_ADDRESS, feedbackNotification.getLocationAddress());
                dataManager.getPreferencesHelper().setValue(Const.FEEDBACK_ID, feedbackNotification.getId());
                break;
            case SCHEDULED_EVENT:
                final EventNotificationFCM eventNotificationScheduled = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                sendNotification(eventNotificationScheduled);
                break;
            case BIG_EVENT:
                final EventNotificationFCM eventNotificationBig = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                sendNotification(eventNotificationBig);
                break;
            case MESSAGE_PRIVATE:
                final MessageNotificationFCM messageNotificationPrivate = mapper.convertValue(remoteMessage.getData(), MessageNotificationFCM.class);
                sendNotificationFromChat(messageNotificationPrivate, false);
                break;
            case MESSAGE_GROUP:
                final MessageNotificationGroupFCM messageNotificationGroup = mapper.convertValue(remoteMessage.getData(), MessageNotificationGroupFCM.class);
                sendNotificationFromChat(messageNotificationGroup, true);
                break;
        }
    }

    private void sendNotificationFromChat(MessageNotificationFCM fcm, boolean isGroup) {
        if(System.currentTimeMillis() - fcm.getTimestamp() > Const.TIME_TO_DROP_NOTIFICATION)
            return;

        if (ChatViewActivity.ACTIVE_ROOM.equals(fcm.getIdRoom()))
            return;

        if (ChatViewActivity.bitmapAvatarFriends.containsKey(fcm.getIdSender()))
            sendNotificationFromChatWithIcon(fcm, ChatViewActivity.bitmapAvatarFriends.get(fcm.getIdSender()), isGroup);
        else
            dataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + fcm.getAvatar())
                    .getBytes(Const.FIVE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        sendNotificationFromChatWithIcon(fcm, bitmap, isGroup);

                    }).addOnFailureListener(exception -> {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
                sendNotificationFromChatWithIcon(fcm, bitmap, isGroup);
            });
    }

    private void sendNotificationFromChatWithIcon(MessageNotificationFCM fcm, Bitmap bitmap, boolean isGroup) {
        Intent intent = new Intent(this, ChatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, fcm.getNameSender());
        ArrayList<CharSequence> idFriend = new ArrayList<>();
        Bitmap avatar = getCircular(bitmap);
        idFriend.add(fcm.getIdSender());
        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, fcm.getIdRoom());
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_NAME, fcm.getRoomName());
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, isGroup);
        ChatViewActivity.bitmapAvatarFriends.put(fcm.getIdSender(), avatar);

        PendingIntent navigationIntent = TaskStackBuilder.create(this)
                .addParentStack(ChatViewActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String nameSender = isGroup ? fcm.getNameSender() + " (" + fcm.getRoomName() + ")" : fcm.getNameSender();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(avatar)
                .setContentTitle(nameSender)
                .setContentText(fcm.getText())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_action_whatshot, getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(fcm.getIdRoom().hashCode(), notificationBuilder.build());
    }

    private void sendNotification(EventNotificationFCM fcm) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("lat", fcm.getLat());
        intent.putExtra("lng", fcm.getLon());
        intent.putExtra("name", fcm.getTitle());
        intent.putExtra("count", 100);

        PendingIntent navigationIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime date = new DateTime(Long.valueOf(fcm.getEndTime()));
        String time = String.format(getString(R.string.notification_time), date.getHourOfDay(), String.format("%02d", date.getMinuteOfHour()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(fcm.getTitle())
                .setContentText(time)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_action_whatshot, getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(fcm.getId().hashCode(), notificationBuilder.build());
    }
}
