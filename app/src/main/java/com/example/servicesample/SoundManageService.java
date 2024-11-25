package com.example.servicesample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

public class SoundManageService extends Service {

    private MediaPlayer _player;
    private static final String CHANNEL_ID = "soundmanagerservice_notification_channel";

    @Override
    public void onCreate() {
        _player = new MediaPlayer();
        String name = getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mediaFileUriStr = "android.resource://" + getPackageName() + "/" + R.raw.suzume;
        Uri mediaFileUri = Uri.parse(mediaFileUriStr);

        try {
            _player.setDataSource(SoundManageService.this, mediaFileUri);
            _player.setOnPreparedListener(new PlayerPreparedListener());
            _player.setOnCompletionListener(new PlayerCompletionListener());
            _player.prepareAsync();

        } catch (IOException ex) {
            Log.e("Servic", "メディアプレーヤー準備時の例外発生", ex);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (_player.isPlaying()) {
            _player.stop();
        }
        _player.release();
    }

    private class PlayerPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(SoundManageService.this, CHANNEL_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentText(getString(R.string.msg_notification_title_start));
            builder.setContentText(getString(R.string.msg_notification_text_start));
            Intent intent = new Intent(SoundManageService.this, MainActivity.class);
            intent.putExtra("fromNotification", true);
            PendingIntent stopServiceIntent
                    = PendingIntent.getActivity(SoundManageService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(stopServiceIntent);
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            startForeground(200, notification);
        }
    }

    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(SoundManageService.this, CHANNEL_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentTitle(getString(R.string.msg_notification_title_finish));
            builder.setContentText(getString(R.string.msg_notification_text_finish));
            Notification notification = builder.build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(SoundManageService.this);
            if (ActivityCompat.checkSelfPermission(SoundManageService.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.notify(100, notification);
            stopSelf();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}