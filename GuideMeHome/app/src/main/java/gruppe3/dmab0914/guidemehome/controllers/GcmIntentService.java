package gruppe3.dmab0914.guidemehome.controllers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            sendNotification(extras.getString("GCMSays"), extras.getString("Arg2"),extras.getString("Name"));
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, String arg2, String name) {
        if (MainActivity.getMainActivity().getmForeground() == false && msg.contains("wants to be guided") || msg.contains("vil guides")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("Message", msg);
            intent.putExtra("Name",name);
            intent.putExtra("Arg2", arg2);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
            Uri defaultSoundUri = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(
                    this).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("GuideMeHome")
                    .setContentText(name + MainActivity.getMainActivity().getString(R.string.wants_to_be_guided_home))
                    .setAutoCancel(true).setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = notificationBuilder.build();

            notificationManager.notify(0, notification);
        } else if (msg.contains("left the route, and needs help!") || msg.contains("forlod ruten, og har brug for hjælp!")) {
            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("GuideMeHome")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(name + " "+ MainActivity.getMainActivity().getString(R.string.left_route_needs_help)))
                            .setContentText(name + " "+ MainActivity.getMainActivity().getString(R.string.left_route_needs_help));
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(1, mBuilder.build());
        }else if (msg.contains("left the route, but is okay") || msg.contains("forlod ruten, men er okay")) {
            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("GuideMeHome")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(name + " "+ MainActivity.getMainActivity().getString(R.string.left_the_route_is_okay)))
                            .setContentText(name + " "+ MainActivity.getMainActivity().getString(R.string.left_the_route_is_okay));
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

}
