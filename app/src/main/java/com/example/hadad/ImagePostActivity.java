package com.example.hadad;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;


import static android.os.Environment.DIRECTORY_DCIM;

public class ImagePostActivity extends AppCompatActivity {

	PhotoView img_post;
	String pImage;
	ImageButton btn_save, btn_delete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_post);

		img_post = findViewById(R.id.img_post);
		btn_save = findViewById(R.id.btn_save);
		btn_delete = findViewById(R.id.btn_delete);
		Intent intent = getIntent();
		pImage = intent.getStringExtra("pImage");
		if(pImage != null)
		{
			try {
				Picasso.get().load(pImage).into(img_post);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		btn_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btn_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uriImage = Uri.parse(pImage);
				saveImage(uriImage);
			}
		});
	}

	private void saveImage(Uri uriImage) {
		String timestamp = System.currentTimeMillis() + "";
		try {
			DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(uriImage);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			request.setDestinationInExternalFilesDir(this, DIRECTORY_DCIM, "IMG_" + timestamp + ".jpg");
			Long reference = downloadManager.enqueue(request);
			Toast.makeText(this, "Picture saved", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
