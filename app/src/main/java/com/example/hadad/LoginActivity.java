package com.example.hadad;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

	private static final int RC_SIGN_IN = 100;
	EditText emailEd, passwordEd;
	Button btn_login;
	TextView not_have_account, recover_pass;
	ProgressDialog progressDialog;
	SignInButton btn_googlelogin;
	GoogleSignInClient mGoogleSignInClient;

	private FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		addControl();
		addEvent();
	}

	private void addEvent() {
		not_have_account.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
				finish();
			}
		});

		btn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = emailEd.getText().toString();
				String pass = passwordEd.getText().toString();
				if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
				{
					emailEd.setError("Invalid email");
					emailEd.setFocusable(true);
				}
				else
				{
					loginUser(email, pass);
				}
			}
		});

		recover_pass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRecoverDialog();

			}
		});

		btn_googlelogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog.setMessage("Logging in with Account Google....");
				progressDialog.show();
				Intent signInIntent = mGoogleSignInClient.getSignInIntent();
				startActivityForResult(signInIntent, RC_SIGN_IN);
			}
		});
	}


	private void addControl() {
		//Actionbar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Login");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3447")));

		//bật chế độ back
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(LoginActivity.this.getResources().getString(R.string.default_web_client_id))
				.requestEmail().build();

		mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

		emailEd = findViewById(R.id.emailEd);
		passwordEd = findViewById(R.id.passwordEd);
		btn_login = findViewById(R.id.btn_login);
		not_have_account = findViewById(R.id.not_have_account);
		recover_pass = findViewById(R.id.recover_pass);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		btn_googlelogin = findViewById(R.id.btn_googlelogin);

		SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
		String emailLast = sharedPreferences.getString("EMAIL_LAST", "None");
		if (!emailLast.equals("None"))
		{
			emailEd.setText(emailLast);
		}

		firebaseAuth = FirebaseAuth.getInstance();
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed(); //quay lại acti trước
		return super.onSupportNavigateUp();
	}

	private void loginUser(String email, String pass) {
		progressDialog.setMessage("Logging in\nPlease wait...");
		progressDialog.show();
		firebaseAuth.signInWithEmailAndPassword(email, pass)
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if(task.isSuccessful()) {
							progressDialog.dismiss();
							SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
							final SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString("EMAIL_LAST", emailEd.getText().toString());
							editor.apply();
							FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
							Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
							startActivity(intent);
							finish();
						}
						else
						{
							progressDialog.dismiss();
							Toast.makeText(LoginActivity.this, "Email or password is failed", Toast.LENGTH_LONG).show();
						}
					}
				})
		.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				progressDialog.dismiss();
				Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
		firebaseAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if(task.isSuccessful())
						{
							progressDialog.dismiss();
							Toast.makeText(LoginActivity.this, "Please check your email !", Toast.LENGTH_LONG).show();
						}
						else
						{
							progressDialog.dismiss();
							Toast.makeText(LoginActivity.this,"Cannot failed", Toast.LENGTH_LONG).show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressDialog.dismiss();
						Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				firebaseAuthWithGoogle(account);
			} catch (ApiException e) {
				// Google Sign In failed, update UI appropriately
				e.printStackTrace();
				// ...
			}
		}
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

		AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
		firebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							progressDialog.dismiss();
							// Sign in success, update UI with the signed-in user's information
							FirebaseUser user = firebaseAuth.getCurrentUser();
							Log.d("is new",task.getResult().getAdditionalUserInfo().isNewUser() + "");
							if(task.getResult().getAdditionalUserInfo().isNewUser())
							{
								String email = user.getEmail();
								String uid = user.getUid();
								String name = user.getDisplayName();


								HashMap<String, Object> hashMap = new HashMap<>();
								hashMap.put("email", email);
								hashMap.put("uid", uid);
								hashMap.put("name", name); //lấy sau
								hashMap.put("onlineStatus", "online");
								hashMap.put("typingTo", "noOne");
								hashMap.put("phone", ""); //lấy sau
								hashMap.put("image", ""); //lấy sau
								hashMap.put("cover", "");
								hashMap.put("subscribers", "");

								FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
								DatabaseReference reference = firebaseDatabase.getReference("Users");
								reference.child(uid).setValue(hashMap);
							}

							Toast.makeText(LoginActivity.this, "user: " + user.getEmail(), Toast.LENGTH_LONG).show();
							Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
							startActivity(intent);
							MainActivity.activity.finish();
							finish();
							//updateUI(user);
						} else {
							// If sign in fails, display a message to the user.
							Toast.makeText(LoginActivity.this, "Login failed..", Toast.LENGTH_LONG).show();
							//updateUI(null);
						}
					}
				})
		.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.uptodown, R.anim.dowtouptran);
	}
}
