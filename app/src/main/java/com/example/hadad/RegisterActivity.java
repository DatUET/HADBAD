package com.example.hadad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hadad.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

	EditText emailEd, passwordEd, firstNameEd, lastNameEd, phoneEd;
	Button btn_register;
	ProgressDialog progressDialog;
	TextView have_account;
	String firstName, lastName, phone = "";
	Boolean isInvalid = true;

	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		addControl();
		addEvent();

	}

	private void addControl() {
		//Actionbar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Create Account");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));

		//bật chế độ back
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		emailEd = findViewById(R.id.emailEd);
		passwordEd = findViewById(R.id.passwordEd);
		firstNameEd = findViewById(R.id.firstNameEd);
		lastNameEd = findViewById(R.id.lastNameEd);
		phoneEd = findViewById(R.id.phoneEd);
		btn_register = findViewById(R.id.btn_register);
		have_account = findViewById(R.id.have_account);
		firebaseAuth = FirebaseAuth.getInstance();
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Registing User...\nPlease wait");
		progressDialog.setCanceledOnTouchOutside(false);
	}

	private void addEvent() {
		emailEd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				final String email = emailEd.getText().toString();
				if(!hasFocus)
					checkEmail(v, email.trim());
			}
		});

		//xử lý nút register
		btn_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isInvalid = true;
				String email = emailEd.getText().toString();
				String pass = passwordEd.getText().toString();
				firstName = firstNameEd.getText().toString();
				lastName = lastNameEd.getText().toString();
				phone = phoneEd.getText().toString();

				if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
				{
					emailEd.setError("Invalid email");
					emailEd.setFocusable(true);
					isInvalid = false;
				}
				if(pass.length() < 6)
				{
					passwordEd.setError("Password length at least 6 characters");
					passwordEd.setFocusable(true);
					isInvalid = false;
				}
				if(TextUtils.isEmpty(firstName))
				{
					firstNameEd.setError("First name is empty");
					firstNameEd.setFocusable(true);
					isInvalid = false;
				}
				if(TextUtils.isEmpty(lastName))
				{
					lastNameEd.setError("Last name is empty");
					lastNameEd.setFocusable(true);
					isInvalid = false;
				}
				if(isInvalid)
				{
					registerUser(email, pass);
				}
			}
		});

		have_account.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	private void checkEmail(View v, final String email) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for(DataSnapshot snapshot : dataSnapshot.getChildren())
				{
					User user = snapshot.getValue(User.class);
					if(user.getEmail().equals(email))
					{
						emailEd.setError("This email is used");
						emailEd.setFocusable(true);
					}
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed(); //quay lại acti trước
		return super.onSupportNavigateUp();
	}

	private void registerUser(String email, String pass) {
		progressDialog.show();

		firebaseAuth.createUserWithEmailAndPassword(email, pass)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if(task.isSuccessful())
						{
							FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
							progressDialog.dismiss();
							//lấy email và uid để đưa lên database
							assert firebaseUser != null;
							String email = firebaseUser.getEmail();
							String uid = firebaseUser.getUid();

							HashMap<String, Object> hashMap = new HashMap<>();
							hashMap.put("email", email);
							hashMap.put("uid", uid);
							hashMap.put("name", firstName + " " + lastName); //lấy sau
							hashMap.put("onlineStatus", "online");
							hashMap.put("typingTo", "noOne");
							hashMap.put("phone", phone); //lấy sau
							hashMap.put("image", ""); //lấy sau
							hashMap.put("cover", "");
							hashMap.put("subscribers", "");

							FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
							DatabaseReference reference = firebaseDatabase.getReference("Users");
							reference.child(uid).setValue(hashMap);
							//
							Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_LONG).show();
							Intent intent = new Intent(RegisterActivity.this, DashBoardActivity.class);
							startActivity(intent);
							finish();
						}
						else
						{
							progressDialog.dismiss();
							Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
						}
					}
				})
		.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				progressDialog.dismiss();
				Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.uptodown, R.anim.dowtouptran);
	}
}
