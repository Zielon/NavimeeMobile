/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.joda.time.DateTime;
import org.pl.android.drively.BoilerplateApplication;
import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.main.MainActivity;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

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

        switch (type) {
            case FEEDBACK:
                dataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK, true);
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_NAME, remoteMessage.getData().get("locationName"));
                dataManager.getPreferencesHelper().setValue(Const.NAME, remoteMessage.getData().get("name"));
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_ADDRESS, remoteMessage.getData().get("locationAddress"));
                dataManager.getPreferencesHelper().setValue(Const.FEEDBACK_ID, remoteMessage.getData().get("id"));
                break;
            case SCHEDULED_EVENT:
                sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("endTime"), remoteMessage.getData().get("lat"), remoteMessage.getData().get("lng"));
                break;
            case BIG_EVENT:
                sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("endTime"), remoteMessage.getData().get("lat"), remoteMessage.getData().get("lng"));
                break;
            case MESSAGE_PRIVATE:
                sendNotificationFromChat(remoteMessage.getData().get("nameSender"), remoteMessage.getData().get("idSender"), remoteMessage.getData().get("text"), remoteMessage.getData().get("avatar"), remoteMessage.getData().get("idRoom"));
                break;
            case MESSAGE_GROUP:
                sendNotificationFromChat(remoteMessage.getData().get("nameSender"), remoteMessage.getData().get("idSender"), remoteMessage.getData().get("text"), remoteMessage.getData().get("avatar"), remoteMessage.getData().get("idRoom"));
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

    private void sendNotificationFromChat(String name, String uderId, String text, String avatarPath, String roomId) {
        if (!ChatViewActivity.active) {
            dataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + avatarPath)
                    .getBytes(Const.FIVE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        sendNotificationFromChatWithIcon(name, text, uderId, bitmap, roomId);

                    }).addOnFailureListener(exception -> {
                Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
                sendNotificationFromChatWithIcon(name, text, uderId, bitmap, roomId);
            });
        }
    }

    private void sendNotificationFromChatWithIcon(String name, String text, String userId, Bitmap bitmap, String roomId) {
        Intent intent = new Intent(this, ChatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, name);
        ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
        idFriend.add(userId);
        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, roomId);
        ChatViewActivity.bitmapAvataFriend = new HashMap<>();
        ChatViewActivity.bitmapAvataFriend.put(userId, bitmap);


        PendingIntent navigationIntent = TaskStackBuilder.create(this)
                .addParentStack(ChatViewActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_d)
                .setColor(getResources().getColor(R.color.primary_dark))
                .setLargeIcon(bitmap)
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
