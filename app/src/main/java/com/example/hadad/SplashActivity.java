package com.example.hadad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    VideoView video_background;
    MediaPlayer mediaPlayer;
    FirebaseUser firebaseUser;
    Animation uptodown;
    ImageView img_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null)
        {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            img_logo = findViewById(R.id.img_logo);
            uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
            img_logo.setAnimation(uptodown);
            video_background = findViewById(R.id.video_background);

            video_background.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.videoback2);
            video_background.requestFocus();
            video_background.setSoundEffectsEnabled(false);
            video_background.start();
            video_background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer = mp;
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            });

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1200);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        Intent intent = new Intent(SplashActivity.this, DashBoardActivity.class);
                        startActivity(intent);
                        finish();

                    }
                }
            });
            thread.start();
        }

    }
}
