/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pl.android.navimee.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.joda.time.DateTime;
import org.pl.android.navimee.BoilerplateApplication;
import org.pl.android.navimee.R;
import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.ui.main.MainActivity;
import org.pl.android.navimee.util.Const;

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
                dataManager.getPreferencesHelper().setValue(Const.IS_FEEDBACK,true);
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_NAME,remoteMessage.getData().get("locationName"));
                dataManager.getPreferencesHelper().setValue(Const.NAME,remoteMessage.getData().get("name"));
                dataManager.getPreferencesHelper().setValue(Const.LOCATION_ADDRESS,remoteMessage.getData().get("locationAddress"));
                dataManager.getPreferencesHelper().setValue(Const.FEEDBACK_ID,remoteMessage.getData().get("id"));
                break;
            case SCHEDULED_EVENT:
                sendNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("endTime"),remoteMessage.getData().get("lat"),remoteMessage.getData().get("lng"));
                break;
            case BIG_EVENT:
                sendNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("endTime"),remoteMessage.getData().get("lat"),remoteMessage.getData().get("lng"));
                break;
        }
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title,String dateTime,String lat, String lng) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("name",title);
        intent.putExtra("count",100);

        PendingIntent navigationIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DateTime date = new DateTime(Long.valueOf(dateTime));
        String time = String.format(getString(R.string.notification_time),date.getHourOfDay(),date.getMinuteOfHour());
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(time)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .addAction(R.drawable.ic_action_whatshot,getResources().getString(R.string.check_in_app), navigationIntent)
                .setContentIntent(navigationIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
