package com.example.hadad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.hadad.Notification.Data;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ChangePassActivity extends AppCompatActivity {

	EditText curentpasswordEd, newpasswordEd, retypepasswordEd;
	TextView recover_pass;
	Button btn_save, btn_cancel;
	FirebaseUser user;
	ProgressDialog progressDialog;
	CircularImageView img_avatar;

	boolean isInvalid = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pass);

		addControl();
		addEvent();
	}

	private void addEvent() {
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btn_save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String currentpass = curentpasswordEd.getText().toString();
				String newpass = newpasswordEd.getText().toString();
				String retypepass = retypepasswordEd.getText().toString();

				if(!TextUtils.isEmpty(newpass) && !TextUtils.isEmpty(retypepass))
				{
					if(!newpass.equals(retypepass))
					{
						retypepasswordEd.setError("Password does not match");
						retypepasswordEd.setFocusable(true);
					}
					else if(newpass.length() < 6)
					{
						newpasswordEd.setError("Password length at least 6 characters");
						newpasswordEd.setFocusable(true);
					}
					else
					{
						updatePass(currentpass, newpass);
					}
				}
			}
		});

		recover_pass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRecoverDialog();

			}
		});
	}

	private void addControl() {
		curentpasswordEd = findViewById(R.id.curentpasswordEd);
		newpasswordEd = findViewById(R.id.newpasswordEd);
		retypepasswordEd = findViewById(R.id.retypepasswordEd);
		recover_pass = findViewById(R.id.recover_pass);
		btn_save = findViewById(R.id.btn_save);
		btn_cancel = findViewById(R.id.btn_cancel);
		img_avatar = findViewById(R.id.img_avatar);
		user = FirebaseAuth.getInstance().getCurrentUser();
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Updating password");
		progressDialog.setMessage("Please wait.....");

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
		progressDialog.show();
		final String email = user.getEmail();
		AuthCredential credential = EmailAuthProvider.getCredential(email,currentpass);

		user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful())
				{
					progressDialog.dismiss();
					user.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if(task.isSuccessful())
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassActivity.this);
								builder.setMessage("Password has been updated");
								builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										finish();
									}
								});
								AlertDialog alertDialog = builder.create();
								alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
								alertDialog.show();
							}
							else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassActivity.this);
								builder.setMessage("Sorry, the password update failed");
								builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								AlertDialog alertDialog = builder.create();
								alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
								alertDialog.show();
							}
						}
					});
				}
				else
				{
					progressDialog.dismiss();
					curentpasswordEd.setError("Current password is incorrect");
					curentpasswordEd.setFocusable(true);
				}
			}
		});
	}

	private void showRecoverDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Recover pass");

		LinearLayout linearLayout = new LinearLayout(this);
		final EditText emailRecoverEd = new EditText(this);
		emailRecoverEd.setHint("Email");
		emailRecoverEd.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		emailRecoverEd.setMinEms(16);
		linearLayout.addView(emailRecoverEd);
		linearLayout.setPadding(10, 10, 10, 10);
		builder.setView(linearLayout);

		builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String email = emailRecoverEd.getText().toString();
				sendRecover(email);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressDialog.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
		alertDialog.show();
	}

	private void sendRecover(String email) {
		progressDialog.setMessage("Sending\nPlease wait...");
		progressDialog.show();
		FirebaseAuth.getInstance().sendPasswordResetEmail(email)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if(task.isSuccessful())
						{
							progressDialog.dismiss();
							Toast.makeText(ChangePassActivity.this, "Please check your email !", Toast.LENGTH_LONG).show();
						}
						else
						{
							progressDialog.dismiss();
							Toast.makeText(ChangePassActivity.this,"Cannot failed", Toast.LENGTH_LONG).show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						Toast.makeText(ChangePassActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}
}
