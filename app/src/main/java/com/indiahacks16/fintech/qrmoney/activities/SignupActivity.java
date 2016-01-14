package com.indiahacks16.fintech.qrmoney.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.indiahacks16.fintech.qrmoney.Contents;
import com.indiahacks16.fintech.qrmoney.QRCodeEncoder;
import com.indiahacks16.fintech.qrmoney.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.Config;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.ServiceErrorException;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import java.io.File;
import java.io.FileOutputStream;

public class SignupActivity extends Activity {
    EditText mUserName;
    EditText mPassword;
    EditText mPhoneNumber;
    Button mAlreadyRegistedButton;
    Button mVerifyButton;
    String TAG = SignupActivity.class.getSimpleName();
    String sinch_app_key="4e3e31be-3c02-48d5-8ded-628cbc4a9b6d";
    String username;
    String password;
    String phoneNumber;
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_signup);
        mUserName = (EditText) findViewById(R.id.name);
        mPassword = (EditText) findViewById(R.id.password);
        mAlreadyRegistedButton = (Button) findViewById(R.id.btnLinkToLoginScreen);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        mVerifyButton = (Button) findViewById(R.id.verifyButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAlreadyRegistedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = mUserName.getText().toString();
                password = mPassword.getText().toString();
                phoneNumber = mPhoneNumber.getText().toString();
                username = username.trim();
                password = password.trim();
                if (username.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                    builder.setMessage(R.string.signup_error_message);
                    builder.setTitle(R.string.signup_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    String number = "+91" + phoneNumber;
                    showProgressDialog();
                    startVerification(number);
                }
            }
        });
    }

    private void startVerification(String phoneNumber) {
        Config config = SinchVerification.config().applicationKey(sinch_app_key).context(getApplicationContext()).build();
        VerificationListener listener = new MyVerificationListener();
        Verification verification = SinchVerification.createFlashCallVerification(config, phoneNumber, listener);
        verification.initiate();
    }

    private void showProgressDialog() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void hideProgressDialog() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    void generateQR(String phoneNumber) {
        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.edit().putString("username", phoneNumber).apply();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class MyVerificationListener implements VerificationListener {
        @Override
        public void onInitiated() {
        }

        @Override
        public void onInitiationFailed(Exception e) {
            hideProgressDialog();
            if (e instanceof InvalidInputException) {
                Snackbar.make(mAlreadyRegistedButton, "Incorrect number provided", Snackbar.LENGTH_LONG).show();
            } else if (e instanceof ServiceErrorException) {
                Snackbar.make(mAlreadyRegistedButton, "Server Error, Try Again Later", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(SignupActivity.this , "Sinch service error", Toast.LENGTH_LONG).show();
            } else {
                Snackbar.make(mAlreadyRegistedButton, "Other system error, check your network state", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(SignupActivity.this, "Other system error, check your network state", Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onVerified() {
            hideProgressDialog();
            new AlertDialog.Builder(SignupActivity.this)
                    .setMessage("Verification Successful!")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                            setProgressBarIndeterminateVisibility(true);
                            ParseUser newUser = new ParseUser();
                            Log.d(TAG, "Username : " + phoneNumber);
                            Log.d(TAG, "Full Name : " + username);
                            Log.d(TAG, "Password : " + password);
                            Log.d(TAG, "Phone Number : " + phoneNumber);
                            newUser.setUsername(phoneNumber);
                            newUser.put("Full_Name", username);
                            newUser.setPassword(password);
                            newUser.put("phoneNumber", phoneNumber);
                            newUser.put("account_balance", 1000);
                            showProgressDialog();
                            newUser.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    hideProgressDialog();
                                    setProgressBarIndeterminateVisibility(false);
                                    if (e == null) {
                                        generateQR(mPhoneNumber.getText().toString().trim());
                                        Intent i = new Intent(SignupActivity.this, MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                                        e.printStackTrace();
                                        builder.setMessage(e.getMessage());
                                        builder.setTitle(R.string.signup_error_title);
                                        builder.setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                        }
                    })
                    .show();
        }
        @Override
        public void onVerificationFailed(Exception e) {
            hideProgressDialog();
            if (e instanceof CodeInterceptionException) {
                Snackbar.make(mAlreadyRegistedButton, "Intercepting the verification call automatically failed", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(SignupActivity.this, "Intercepting the verification call automatically failed", Toast.LENGTH_LONG).show();
            } else if (e instanceof ServiceErrorException) {
                Snackbar.make(mAlreadyRegistedButton, "Internal Server Error", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(SignupActivity.this, "Sinch service error", Toast.LENGTH_LONG).show();
            } else {
                Snackbar.make(mAlreadyRegistedButton, "Other system error, check your network state", Snackbar.LENGTH_LONG).show();
                //Toast.makeText(SignupActivity.this, "Other system error, check your network state", Toast.LENGTH_LONG).show();
            }
        }

    }
}
