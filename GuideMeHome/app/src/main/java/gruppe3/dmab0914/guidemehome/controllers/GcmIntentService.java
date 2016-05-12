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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import gruppe3.dmab0914.guidemehome.BuildConfig;
import gruppe3.dmab0914.guidemehome.R;
import gruppe3.dmab0914.guidemehome.activities.MainActivity;
import gruppe3.dmab0914.guidemehome.fragments.DrawRouteFragment;

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
            sendNotification(extras.getString("GCMSays"));
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("Message", msg);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
    Uri defaultSoundUri = RingtoneManager
            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(
            this).setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("GuideMeHome").setContentText(msg)
            .setAutoCancel(true).setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = notificationBuilder.build();

    notificationManager.notify(0, notification);
}

    }
