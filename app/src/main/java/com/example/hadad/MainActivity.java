package com.example.hadad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

	Button btn_login, btn_register;
	MediaPlayer mediaPlayer;
	public static Activity activity;
	ImageView img_logo;
	Animation uptodown, downtoup, downtouptran, uptodowntran;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_main);

		btn_login = findViewById(R.id.btn_login);
		btn_register = findViewById(R.id.btn_register);
		img_logo = findViewById(R.id.img_logo);
		uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
		downtoup = AnimationUtils.loadAnimation(this, R.anim.dowtoup);
		uptodowntran = AnimationUtils.loadAnimation(this, R.anim.uptodowntran);
		downtouptran = AnimationUtils.loadAnimation(this, R.anim.dowtouptran);

		img_logo.setAnimation(uptodown);
		btn_login.setAnimation(downtoup);
		btn_register.setAnimation(downtoup);

		btn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.uptodown, R.anim.dowtouptran);
			}
		});

		btn_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.uptodown, R.anim.dowtouptran);
			}
		});
	}
}
