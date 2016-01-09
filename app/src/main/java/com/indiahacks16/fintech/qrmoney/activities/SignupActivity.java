package com.indiahacks16.fintech.qrmoney.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.indiahacks16.fintech.qrmoney.Contents;
import com.indiahacks16.fintech.qrmoney.QRCodeEncoder;
import com.indiahacks16.fintech.qrmoney.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;
import java.io.FileOutputStream;

public class SignupActivity extends Activity {
    EditText mUserName;
    EditText mPassword;
    EditText mEmail;
    EditText mPhoneNumber;
    Button mSignUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_signup);
        mUserName = (EditText) findViewById(R.id.name);
        mPassword = (EditText) findViewById(R.id.password);
        mEmail = (EditText) findViewById(R.id.email);
        mSignUpButton = (Button) findViewById(R.id.btnRegister);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUserName.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String phoneNumber = mPhoneNumber.getText().toString();
                username = username.trim();
                password = password.trim();
                email = email.trim();
                phoneNumber = phoneNumber.trim();
                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                    builder.setMessage(R.string.signup_error_message);
                    builder.setTitle(R.string.signup_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    setProgressBarIndeterminateVisibility(true);
                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            if (e == null) {
                                generateQR(mPhoneNumber.getText().toString().trim());
                                Intent i = new Intent(SignupActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                                builder.setMessage(e.getMessage());
                                builder.setTitle(R.string.signup_error_title);
                                builder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
    }
    void generateQR(String phoneNumber) {
        phoneNumber = "$$" + phoneNumber + "##";
        Log.v(this.getClass().getSimpleName(), phoneNumber);
        QRCodeEncoder qrCodeEncoder =
                new QRCodeEncoder(phoneNumber, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 300);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            String filePath = Environment.getExternalStorageDirectory()
                    + "/qrmoney";
            File dir = new File(filePath);
            if(!dir.exists())
                dir.mkdirs();
            filePath += "/" + phoneNumber + ".png";
            File file = new File(filePath);
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            SharedPreferences sp = getSharedPreferences("PHNO", MODE_PRIVATE);
            sp.edit().putString("phno", phoneNumber).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
