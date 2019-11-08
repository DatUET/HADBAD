package com.example.hadad.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.hadad.ChatActivity;
import com.example.hadad.PostDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {

	boolean enable4NewPost;
	SharedPreferences preferences;
	private static final String TOPIC_POST_NOTI = "POST";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);

		SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
		String savedCurrentUser = sharedPreferences.getString("Current_USER", "None");

		preferences = getSharedPreferences("NotiPost", MODE_PRIVATE);
		enable4NewPost = preferences.getBoolean(TOPIC_POST_NOTI, false);

		String sent = remoteMessage.getData().get("sent");
		String user = remoteMessage.getData().get("user");
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if(firebaseUser != null && sent.equals(firebaseUser.getUid()))
		{
			if(!savedCurrentUser.equals(user))
			{
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				{
					sendOAboveNotification(remoteMessage);
				}
				else
				{
					sendNormalNotification(remoteMessage);
				}
			}
		}
	}

	private void sendNormalNotification(RemoteMessage remoteMessage) {
		String user = remoteMessage.getData().get("user");
		String title = remoteMessage.getData().get("title");
		String body = remoteMessage.getData().get("body");
		String key = remoteMessage.getData().get("key");
		String icon = remoteMessage.getData().get("icon");

		RemoteMessage.Notification notification = remoteMessage.getNotification();
		int i =Integer.parseInt(user.replaceAll("[\\D]", ""));
		Intent intent;
		if(key.equals("chat"))
		{
			intent = new Intent(this, ChatActivity.class);
		}
		else
		{
			intent = new Intent(this, PostDetailActivity.class);
		}
		Bundle bundle = new Bundle();
		bundle.putString("uid", user);
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if(key.equals("chat") || enable4NewPost) {
			Notification.Builder builder = new Notification.Builder(this)
					.setSmallIcon(Integer.parseInt(icon))
					.setContentTitle(title)
					.setContentText(body)
					.setSound(defSoundUri)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent);

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			int j = 0;
			if (i > 0) {
				j = i;
			}
			notificationManager.notify(j, builder.build());
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void sendOAboveNotification(RemoteMessage remoteMessage) {
		String user = remoteMessage.getData().get("user");
		String title = remoteMessage.getData().get("title");
		String body = remoteMessage.getData().get("body");
		String key = remoteMessage.getData().get("key");
		String icon = remoteMessage.getData().get("icon");

		RemoteMessage.Notification notification = remoteMessage.getNotification();

		if(key.equals("chat"))
		{
			int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
			Intent intent = new Intent(this, ChatActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("uid", user);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

			Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			OreoAboveNotification oreoAboveNotification = new OreoAboveNotification(this);
			Notification.Builder builder = oreoAboveNotification.getONotification(title, body, pendingIntent, defSoundUri, icon);

			int j = 0;
			if(i>0)
			{
				j = i;
			}
			oreoAboveNotification.getManager().notify(j, builder.build());
		}
		else if(key.equals("comment"))
		{
			int i = Integer.parseInt((System.currentTimeMillis() - Long.parseLong(user)) + "");
			Intent intent = new Intent(this, PostDetailActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("postId", user);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

			Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			OreoAboveNotification oreoAboveNotification = new OreoAboveNotification(this);
			Notification.Builder builder = oreoAboveNotification.getONotification(title, body, pendingIntent, defSoundUri, icon);

			int j = 0;
			if(i>0)
			{
				j = i;
			}
			oreoAboveNotification.getManager().notify(j, builder.build());
		}
		else if(key.equals("newpost") && enable4NewPost)
		{
			int i = Integer.parseInt((System.currentTimeMillis() - Long.parseLong(user)) + "");
			Intent intent = new Intent(this, PostDetailActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("postId", user);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

			Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			OreoAboveNotification oreoAboveNotification = new OreoAboveNotification(this);
			Notification.Builder builder = oreoAboveNotification.getONotification(title, body, pendingIntent, defSoundUri, icon);

			int j = 0;
			if(i>0)
			{
				j = i;
			}
			oreoAboveNotification.getManager().notify(j, builder.build());
		}

	}

	@Override
	public void onNewToken(@NonNull String s) {
		super.onNewToken(s);

		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if(firebaseUser != null)
		{
			updateToken(s);
		}
	}

	private void updateToken(String s) {
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
		Token token = new Token(s);
		ref.child(firebaseUser.getUid()).setValue(token);

	}
}
