package com.example.hadad;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import static android.os.Environment.DIRECTORY_DCIM;

public class VideoViewActivity extends AppCompatActivity {

    VideoView video_view;
    String url;
    Uri uri;
    MediaController mediaController;

    ImageButton btn_save, btn_delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        video_view = findViewById(R.id.video_view);
        btn_save = findViewById(R.id.btn_save);
        btn_delete = findViewById(R.id.btn_delete);
        url = getIntent().getStringExtra("url");
        uri = Uri.parse(url);
        video_view.setVideoURI(uri);
        mediaController = new MediaController(VideoViewActivity.this);
        mediaController.setAnchorView(video_view);
        video_view.setMediaController(mediaController);
        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                mediaController.setAnchorView(video_view);
                video_view.setMediaController(mediaController);
            }
        });
        video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                video_view.start();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri!=null)
                {
                    saveVideo(uri);
                }
                else
                {
                    Toast.makeText(VideoViewActivity.this, "No Video", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveVideo(Uri uriVideo) {
        String timestamp = System.currentTimeMillis() + "";
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uriVideo);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(this, DIRECTORY_DCIM, "VID_" + timestamp + ".mp4");
            Long reference = downloadManager.enqueue(request);
            Toast.makeText(this, "Video saved", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
