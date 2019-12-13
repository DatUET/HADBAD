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
