package com.indiahacks16.fintech.qrmoney.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.indiahacks16.fintech.qrmoney.Contents;
import com.indiahacks16.fintech.qrmoney.QRCodeEncoder;
import com.indiahacks16.fintech.qrmoney.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AskMoneyActivity extends AppCompatActivity {
    Button gallery, camera, send;
    TextView availableBalance;
    ImageView qrImage;
    TextView receiverInfo;
    EditText amountInput;
    String receiverPhoneNumber;
    String user;
    float requestAmount;
    int n = 0 ;
    String TAG = this.getClass().getSimpleName();
    String userFullName;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final int FROM_GALLERY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_money);
        gallery = (Button) findViewById(R.id.gallery_ask);
        camera = (Button) findViewById(R.id.camera_ask);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrUsingGallery();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrUsingCamera();
            }
        });
        qrImage = (ImageView) findViewById(R.id.qr_image_ask);
        receiverInfo = (TextView) findViewById(R.id.receiver_info_ask);
        ParseUser user1 = ParseUser.getCurrentUser();
        final int accountBalance = (int) user1.get("account_balance");
        availableBalance = (TextView) findViewById(R.id.available_balance_ask);
        availableBalance.setText("RS. " + accountBalance);
        amountInput = (EditText) findViewById(R.id.amount_ask);
        send = (Button) findViewById(R.id.send_button_ask);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(amountInput.getText().toString());
                Log.v(this.getClass().getSimpleName(), "Inside Listener");
                if (amount > accountBalance) {
                    Snackbar.make(send, "Inadequate Balance!!!", Snackbar.LENGTH_LONG).show();
                    Log.v(this.getClass().getSimpleName(), "Inadequate amount");
                }
                else {
                    String loginNumber = getSharedPreferences("LOGIN", MODE_PRIVATE).getString("username", "");
                    requestMoney(receiverPhoneNumber, loginNumber, requestAmount);
                }
            }
        });
    }

    void requestMoney(final String receiverPhoneNumber, final String loginNumber, final float requestAmount) {
        final ParseQuery<ParseInstallation> query2 = ParseInstallation.getQuery();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", receiverPhoneNumber);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    Log.v(this.getClass().getSimpleName(), "## " + object.getString("Full_Name"));
                    Log.v(this.getClass().getSimpleName(), "$$ " + object.getObjectId());
                    query2.whereEqualTo("objectId", object.getObjectId());
                    ParsePush push = new ParsePush();
                    push.setQuery(query2);
                    Log.v(this.getClass().getSimpleName(), "Push Query Set");
                    JSONObject data = new JSONObject();
                    try {
                        data.put("sender", loginNumber);
                        data.put("amount", requestAmount);
                    } catch (JSONException e1) {
                        Log.v(this.getClass().getSimpleName(), "Push Data error");
                        e1.printStackTrace();
                    }
                    push.setData(data);
                    Log.v(this.getClass().getSimpleName(), "Push Data Set");
                    push.sendInBackground(new SendCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null)
                                Log.v(this.getClass().getSimpleName(), "Push successfully sent");
                            else
                                Log.v(this.getClass().getSimpleName(), "Error : " + e.getMessage());
                        }
                    });
                } else {
                    Log.v(this.getClass().getSimpleName(), "Some error");
                }
            }
        });
    }
    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            send.setEnabled(true);
            if(requestCode == FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                receiverPhoneNumber = decodeQr(selectedImageUri);
            }
            else {
                receiverPhoneNumber = data.getStringExtra(ZBarConstants.SCAN_RESULT);
                receiverPhoneNumber = receiverPhoneNumber.substring(2, receiverPhoneNumber.length() - 2);
                qrImage.setImageBitmap(generateQR(data.getStringExtra(ZBarConstants.SCAN_RESULT)));
            }
            ParseQuery query = ParseUser.getQuery();
            query.whereEqualTo("username", receiverPhoneNumber);
            final String finalPhoneNumber = receiverPhoneNumber;
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        ParseUser p = user;
                        String full_name = (String) user.get("Full_Name");
                        Log.d(TAG, "Full name of user : " + full_name);
                        int amount = Integer.parseInt(amountInput.getText().toString());
                        requestAmount = Float.parseFloat(amountInput.getText().toString());
                        int receiver_account_balance = user.getInt("account_balance");
                        int sender_account_balance = ParseUser.getCurrentUser().getInt("account_balance");
                        receiver_account_balance += amount;
                        sender_account_balance -= amount;
                        String s = (String) ParseUser.getCurrentUser().get("Full_Name");
                        ParseObject transaction_record = new ParseObject("Transaction_Record");
                        transaction_record.put("Sender_Name", s);
                        transaction_record.put("Sender_Number", ParseUser.getCurrentUser().getUsername());
                        transaction_record.put("Receiver_Name", full_name);
                        userFullName = full_name;
                        transaction_record.put("Receiver_Number", receiverPhoneNumber);
                        transaction_record.put("Amount", amount);
                        transaction_record.put("Transcation_Status", "0"); //Status = 0 means transaction incomplete.
                        transaction_record.saveInBackground();
                        user.put("account_balance", receiver_account_balance);
                        user.saveInBackground();
                        p.put("account_balance", receiver_account_balance);
                        p.saveInBackground();
                        ParseUser.getCurrentUser().put("account_balance", sender_account_balance);
                        ParseUser.getCurrentUser().saveInBackground();
                        receiverInfo.setText(getResources().getString(R.string.receiver) + full_name + "\n" + getResources().getString(R.string.thanks_msg));
                        n = sender_account_balance;
                        availableBalance.setText("" + sender_account_balance);
                        if (user == null) {
                            Log.d(TAG, "No matching user. ");
                        } else {
                            full_name = (String) user.get("Full_Name");
                            user.put("account_balance", receiver_account_balance);
                            user.saveInBackground();
                            receiverInfo.setText(getResources().getString(R.string.receiver) + full_name + "\n" + getResources().getString(R.string.thanks_msg));
                        }
                    } else {
                        Log.d(TAG, "Exception : " + e);
                    }
                }
            });
        }
    }
    Bitmap generateQR(String phoneNumber) {
        phoneNumber = "$$" + phoneNumber + "##";
        QRCodeEncoder qrCodeEncoder =
                new QRCodeEncoder(phoneNumber, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 300);
        try {
            return  qrCodeEncoder.encodeAsBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    String decodeQr(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(bBitmap);
                Picasso.with(getApplicationContext()).load(uri).into(qrImage);
                String number = result.getText();
                number = number.substring(2, number.length() - 2);
                return number;
            }
            catch (Exception e) {
                e.printStackTrace();
                return "QR Decoding Failed";
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return "QR Decoding Failed";
        }
    }
    void qrUsingGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), FROM_GALLERY);
    }
    void qrUsingCamera() {
        Intent intent = new Intent(getApplicationContext(),
                ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }
}
