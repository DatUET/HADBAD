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
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hadad.Model.User;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

	private static final int RC_SIGN_IN = 100;
	EditText emailEd, passwordEd;
	Button btn_login;
	TextView not_have_account, recover_pass;
	SweetAlertDialog sweetAlertDialog;
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

	private void addControl() {
		//Actionbar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Login");
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1A1A1A")));

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
		sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		sweetAlertDialog.setCanceledOnTouchOutside(false);
		btn_googlelogin = findViewById(R.id.btn_googlelogin);

		SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
		String emailLast = sharedPreferences.getString("EMAIL_LAST", "None");
		if (!emailLast.equals("None")) {
			emailEd.setText(emailLast);
		}

		firebaseAuth = FirebaseAuth.getInstance();
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
				if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(email)) {
					emailEd.setError("Invalid email");
					emailEd.setFocusable(true);
				} else if (TextUtils.isEmpty(pass)) {
					passwordEd.setError("Invalid password");
					passwordEd.setFocusable(true);
				} else {
					loginUser(email, pass);
				}
			}
		});


		emailEd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				final String email = emailEd.getText().toString();
				if (!hasFocus) {
					if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
						emailEd.setError("Invalid email");
						emailEd.setFocusable(true);
					} else {
						checkEmail(v, email.trim());
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

		btn_googlelogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sweetAlertDialog.setTitleText("Logging in with Account Google....");
				sweetAlertDialog.show();
				Intent signInIntent = mGoogleSignInClient.getSignInIntent();
				startActivityForResult(signInIntent, RC_SIGN_IN);
			}
		});
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed(); //quay lại acti trước
		return super.onSupportNavigateUp();
	}

	private void loginUser(String email, String pass) {
		sweetAlertDialog.setTitleText("Logging in\nPlease wait...");
		sweetAlertDialog.show();
		firebaseAuth.signInWithEmailAndPassword(email, pass)
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							sweetAlertDialog.dismiss();
							SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
							final SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString("EMAIL_LAST", emailEd.getText().toString());
							editor.apply();
							FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
							if (firebaseUser.isEmailVerified()) {
								Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
								startActivity(intent);
								finish();
							} else {
								sweetAlertDialog.dismiss();
								new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE)
										.setTitleText("Notification!")
										.setContentText("Please verify your email address!")
										.show();
							}
						} else {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("ERROR")
									.setContentText("Email or password is failed")
									.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
										@Override
										public void onClick(SweetAlertDialog sweetAlertDialog) {
											sweetAlertDialog.dismiss();
										}
									})
									.show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						sweetAlertDialog.dismiss();
						new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
								.setTitleText("ERROR")
								.setContentText(e.getMessage())
								.show();
					}
				});
	}

	private void showRecoverDialog() {
		new LovelyTextInputDialog(this)
				.setTopColor(Color.parseColor("#40C4FF"))
				.setTitle("Recover pass")
				.setMessage("Please enter your email")
				.setInputFilter("Invalid email", new LovelyTextInputDialog.TextFilter() {
					@Override
					public boolean check(String text) {
						return !TextUtils.isEmpty(text.trim()) || Patterns.EMAIL_ADDRESS.matcher(text.trim()).matches();
					}
				})
				.setConfirmButton("Recover", new LovelyTextInputDialog.OnTextInputConfirmListener() {
					@Override
					public void onTextInputConfirmed(String text) {
						sendRecover(text.trim());
					}
				}).show();
	}

	private void sendRecover(String email) {
		sweetAlertDialog.setTitleText("Sending\nPlease wait...");
		sweetAlertDialog.show();
		firebaseAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE)
									.setTitleText("SUCCESS!")
									.setContentText("Please check your email !")
									.show();
							//Toast.makeText(LoginActivity.this, "Please check your email !", Toast.LENGTH_LONG).show();
						} else {
							sweetAlertDialog.dismiss();
							new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("SORRY!")
									.setContentText("Error! An error occurred. Please try again later")
									.show();

							//Toast.makeText(LoginActivity.this,"Cannot failed", Toast.LENGTH_LONG).show();
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						sweetAlertDialog.dismiss();
						new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
								.setTitleText("SORRY!")
								.setContentText("Error! An error occurred. Please try again later")
								.show();
						//Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
	}

	private void checkEmail(View v, final String email) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				boolean isRegisted = false;
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					User user = snapshot.getValue(User.class);
					if (user.getEmail().equals(email)) {
						isRegisted = true;
					}
				}
				if (!isRegisted) {
					emailEd.setError("This email has not been registered");
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

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
							sweetAlertDialog.dismiss();
							// Sign in success, update UI with the signed-in user's information
							FirebaseUser user = firebaseAuth.getCurrentUser();
							if (task.getResult().getAdditionalUserInfo().isNewUser()) {
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
							new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("OOP...!")
									.setContentText("Login failed..")
									.show();
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
