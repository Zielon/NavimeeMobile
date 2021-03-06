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
import java.util.HashMap;

import javax.inject.Inject;

import static org.pl.android.drively.util.BitmapUtils.getCircular;
import static org.pl.android.drively.util.FirebasePaths.AVATARS;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    DataManager dataManager;

    // The cashing is performed only in the life time of the service
    private HashMap<String, Bitmap> avatars = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Const.NotificationsType type = Const.NotificationsType.valueOf(remoteMessage.getData().get("type"));
            final ObjectMapper mapper = new ObjectMapper();
            switch (type) {
                case FEEDBACK:
                    final FeedbackNotificationFCM feedback = mapper.convertValue(remoteMessage.getData(), FeedbackNotificationFCM.class);
                    setFeedback(feedback);
                    break;
                case SCHEDULED_EVENT:
                    final EventNotificationFCM event = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                    sendNotification(event);
                    break;
                case BIG_EVENT:
                    final EventNotificationFCM eventBig = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                    sendNotification(eventBig);
                    break;
                case MESSAGE_PRIVATE:
                    final MessageNotificationFCM privateMessage = mapper.convertValue(remoteMessage.getData(), MessageNotificationFCM.class);
                    sendNotificationFromChat(privateMessage, false);
                    break;
                case MESSAGE_GROUP:
                    final MessageNotificationGroupFCM groupMessage = mapper.convertValue(remoteMessage.getData(), MessageNotificationGroupFCM.class);
                    sendNotificationFromChat(groupMessage, true);
                    break;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void setFeedback(FeedbackNotificationFCM feedbackNotification) {
        dataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK, true);
        dataManager.getPreferencesHelper().setValue(Const.LOCATION_NAME, feedbackNotification.getLocationName());
        dataManager.getPreferencesHelper().setValue(Const.NAME, feedbackNotification.getName());
        dataManager.getPreferencesHelper().setValue(Const.LOCATION_ADDRESS, feedbackNotification.getLocationAddress());
        dataManager.getPreferencesHelper().setValue(Const.FEEDBACK_ID, feedbackNotification.getId());
    }

    private void sendNotificationFromChat(MessageNotificationFCM fcm, boolean isGroup) {
        if (dataManager.getFirebaseService().getFirebaseAuth().getCurrentUser() == null) return;
        if (ChatViewActivity.ACTIVE_ROOM.equals(fcm.getIdRoom()) || fcm.getIdRoom() == null) return;
        if (fcm.getIdSender().equals(dataManager.getPreferencesHelper().getUserId())) return;
        if (System.currentTimeMillis() - fcm.getTimestamp() > Const.TIME_TO_DROP_NOTIFICATION)
            return;

        if (avatars.containsKey(fcm.getIdSender()))
            sendNotificationFromChatWithIcon(fcm, avatars.get(fcm.getIdSender()), isGroup);
        else
            dataManager.getFirebaseService().getFirebaseStorage().getReference(AVATARS + "/" + fcm.getIdSender())
                    .getBytes(Const.FIVE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        avatars.put(fcm.getIdSender(), bitmap);
                        sendNotificationFromChatWithIcon(fcm, bitmap, isGroup);
                    }).addOnFailureListener(exception -> {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
                avatars.put(fcm.getIdSender(), bitmap);
                sendNotificationFromChatWithIcon(fcm, bitmap, isGroup);
            });
    }

    private void sendNotificationFromChatWithIcon(MessageNotificationFCM fcm, Bitmap bitmap, boolean isGroup) {

        Intent intent = new Intent(this, ChatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, fcm.getNameSender());
        ArrayList<CharSequence> idFriend = new ArrayList<>();
        Bitmap avatar = getCircular(bitmap, 200, 200);
        idFriend.add(fcm.getIdSender());

        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, fcm.getIdRoom());
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_NAME, fcm.getRoomName());
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, isGroup);

        PendingIntent navigationIntent = TaskStackBuilder.create(this)
                .addParentStack(ChatViewActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification message = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(avatar)
                .setContentTitle(fcm.getNameSender())
                .setContentText(fcm.getText())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setGroup(fcm.getIdRoom())
                .addAction(R.drawable.ic_action_whatshot, getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent).build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        Notification summary =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_d)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .setSummaryText(fcm.getRoomName()))
                        .setGroup(fcm.getIdRoom())
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        notificationManager.notify(fcm.getIdRoom().hashCode(), summary);
        notificationManager.notify((fcm.getIdSender() + fcm.getIdRoom() + fcm.getNameSender()).hashCode(), message);
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
        if (notificationManager != null)
            notificationManager.notify(fcm.getId().hashCode(), notificationBuilder.build());
    }
}
