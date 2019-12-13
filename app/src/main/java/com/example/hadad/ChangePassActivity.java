package com.example.hadad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangePassActivity extends AppCompatActivity {

	EditText curentpasswordEd, newpasswordEd, retypepasswordEd;
	Button btn_save, btn_cancel;
	FirebaseUser user;
	SweetAlertDialog sweetAlertDialog;
	CircularImageView img_avatar;
	boolean isGoogleAcc = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pass);

		addControl();
		addEvent();
	}

	private void addEvent() {
		for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
			if(userInfo.getProviderId().equals("google.com"))
			{
				isGoogleAcc = true;
			}
		}
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isGoogleAcc) {
					new SweetAlertDialog(ChangePassActivity.this, SweetAlertDialog.ERROR_TYPE)
							.setTitleText("Sorry!")
							.setContentText("Your account is Google account, so you can't change password")
							.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									finish();
								}
							})
							.show();
				}
				else {
					String currentpass = curentpasswordEd.getText().toString();
					String newpass = newpasswordEd.getText().toString();
					String retypepass = retypepasswordEd.getText().toString();

					if (!TextUtils.isEmpty(newpass) && !TextUtils.isEmpty(retypepass)) {
						if (!newpass.equals(retypepass)) {
							retypepasswordEd.setError("Password does not match");
							retypepasswordEd.setFocusable(true);
						} else if (newpass.length() < 6) {
							newpasswordEd.setError("Password length at least 6 characters");
							newpasswordEd.setFocusable(true);
						} else {
							updatePass(currentpass, newpass);
						}
					}
				}
			}
		});
	}

	private void addControl() {
		curentpasswordEd = findViewById(R.id.curentpasswordEd);
		newpasswordEd = findViewById(R.id.newpasswordEd);
		retypepasswordEd = findViewById(R.id.retypepasswordEd);
		btn_save = findViewById(R.id.btn_save);
		btn_cancel = findViewById(R.id.btn_cancel);
		img_avatar = findViewById(R.id.img_avatar);
		user = FirebaseAuth.getInstance().getCurrentUser();
		sweetAlertDialog = new SweetAlertDialog(this);

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("image");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(!TextUtils.isEmpty(dataSnapshot.getValue().toString()))
					Picasso.get().load(dataSnapshot.getValue() + "").networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(img_avatar);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void updatePass(String currentpass, final String newpass) {
		sweetAlertDialog.setTitleText("Updating password");
		sweetAlertDialog.setContentText("Please wait.....");
		sweetAlertDialog.show();
		final String email = user.getEmail();
		AuthCredential credential = EmailAuthProvider.getCredential(email,currentpass);

		user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful())
				{
					sweetAlertDialog.dismiss();
					user.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if(task.isSuccessful())
							{
								new SweetAlertDialog(ChangePassActivity.this, SweetAlertDialog.SUCCESS_TYPE)
										.setTitleText("Password has been updated")
										.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(SweetAlertDialog sweetAlertDialog) {
												sweetAlertDialog.dismiss();
												finish();
											}
										})
										.show();
							}
							else
							{
								new SweetAlertDialog(ChangePassActivity.this, SweetAlertDialog.ERROR_TYPE)
										.setTitleText("Sorry, the password update failed")
										.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(SweetAlertDialog sweetAlertDialog) {
												sweetAlertDialog.dismissWithAnimation();
											}
										})
										.show();
							}
						}
					});
				}
				else
				{
					sweetAlertDialog.dismiss();
					curentpasswordEd.setError("Current password is incorrect");
					curentpasswordEd.setFocusable(true);
				}
			}
		});
	}
}
