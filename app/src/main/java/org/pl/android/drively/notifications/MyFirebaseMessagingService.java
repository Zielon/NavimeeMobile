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
import android.util.Log;

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


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Inject
    DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        BoilerplateApplication.get(this).getComponent().inject(this);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Handle message within 10 seconds
            handleNow();
        }

        // Check if message contains a notification payload.
        //if (remoteMessage.getNotification() != null) {
        Log.d(TAG, "Data manager in service: " + dataManager.toString());
        Const.NotificationsType type = Const.NotificationsType.valueOf(remoteMessage.getData().get("type"));
        final ObjectMapper mapper = new ObjectMapper();
        switch (type) {
            case FEEDBACK:
               // jackson's objectmapper
                final FeedbackNotificationFCM feedbackNotification = mapper.convertValue(remoteMessage.getData(), FeedbackNotificationFCM.class);
                dataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK, true);
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_NAME, feedbackNotification.getLocationName());
                dataManager.getPreferencesHelper().setValue(Const.NAME, feedbackNotification.getName());
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_ADDRESS, feedbackNotification.getLocationAddress());
                dataManager.getPreferencesHelper().setValue(Const.FEEDBACK_ID, feedbackNotification.getId());
                break;
            case SCHEDULED_EVENT:
                final EventNotificationFCM eventNotificationScheduled = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                sendNotification(eventNotificationScheduled.getTitle(), eventNotificationScheduled.getEndTime(), eventNotificationScheduled.getLat(), eventNotificationScheduled.getLng());
                break;
            case BIG_EVENT:
                final EventNotificationFCM eventNotificationBig = mapper.convertValue(remoteMessage.getData(), EventNotificationFCM.class);
                sendNotification(eventNotificationBig.getTitle(), eventNotificationBig.getEndTime(), eventNotificationBig.getLat(), eventNotificationBig.getLng());
                break;
            case MESSAGE_PRIVATE:
                final MessageNotificationFCM messageNotificationPrivate = mapper.convertValue(remoteMessage.getData(), MessageNotificationFCM.class);
                sendNotificationFromChat(messageNotificationPrivate.getNameSender(), messageNotificationPrivate.getIdSender(), messageNotificationPrivate.getText(),
                        messageNotificationPrivate.getAvatar(), messageNotificationPrivate.getIdRoom(),false);
                break;
            case MESSAGE_GROUP:
                final MessageNotificationGroupFCM messageNotificationGroup = mapper.convertValue(remoteMessage.getData(), MessageNotificationGroupFCM.class);
                sendNotificationFromChat(messageNotificationGroup.getName(), messageNotificationGroup.getIdSender(), messageNotificationGroup.getText(),
                        messageNotificationGroup.getAvatar(), messageNotificationGroup.getIdRoom(),true);
                break;
        }
    }


    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendNotificationFromChat(String name, String uderId, String text, String avatarPath, String roomId, boolean isGroup) {
        if (!ChatViewActivity.active) {
            dataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + avatarPath)
                    .getBytes(Const.FIVE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        sendNotificationFromChatWithIcon(name, text, uderId, bitmap, roomId, isGroup);

                    }).addOnFailureListener(exception -> {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
                sendNotificationFromChatWithIcon(name, text, uderId, bitmap, roomId, isGroup);
            });
        }
    }

    private void sendNotificationFromChatWithIcon(String name, String text, String userId, Bitmap bitmap, String roomId, boolean isGroup) {
        Intent intent = new Intent(this, ChatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, name);
        ArrayList<CharSequence> idFriend = new ArrayList<>();
        Bitmap avatar = getCircular(bitmap);
        idFriend.add(userId);
        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, roomId);
        intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, isGroup);
        ChatViewActivity.bitmapAvataFriend = new HashMap<>();
        ChatViewActivity.bitmapAvataFriend.put(userId, avatar);

        PendingIntent navigationIntent = TaskStackBuilder.create(this)
                .addParentStack(ChatViewActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(avatar)
                .setContentTitle(name)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_action_whatshot, getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(roomId), notificationBuilder.build());
    }


    // [END rec

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String title, String dateTime, String lat, String lng) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("name", title);
        intent.putExtra("count", 100);

        PendingIntent navigationIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime date = new DateTime(Long.valueOf(dateTime));
        String time = String.format(getString(R.string.notification_time), date.getHourOfDay(), String.format("%02d", date.getMinuteOfHour()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(time)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_action_whatshot, getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}
