package com.indiahacks16.fintech.qrmoney.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.zxing.BarcodeFormat;
import com.indiahacks16.fintech.qrmoney.Contents;
import com.indiahacks16.fintech.qrmoney.QRCodeEncoder;
import com.indiahacks16.fintech.qrmoney.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

public class SendMoneyFragment extends Fragment {
    String TAG = SendMoneyFragment.class.getSimpleName();
    Button gallery, camera, send;
    ImageView qrImage;
    TextView receiverInfo, availableBalance;
    EditText amountInput;
    String user;
    int n = 0 ;
    File historyFile;
    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final int FROM_GALLERY = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_money, container, false);
        gallery = (Button) v.findViewById(R.id.gallery);
        camera = (Button) v.findViewById(R.id.camera);
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
        historyFile = new File(Environment.getExternalStorageDirectory() + "/qrmoney/history/" +
                getContext().getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("username", "") + ".json");
        qrImage = (ImageView) v.findViewById(R.id.qr_image);
        receiverInfo = (TextView) v.findViewById(R.id.receiver_info);
        ParseUser user1 = ParseUser.getCurrentUser();
        final int accountBalance = (int) user1.get("account_balance");
        availableBalance = (TextView) v.findViewById(R.id.available_balance);
        availableBalance.setText("RS. " + accountBalance);
        amountInput = (EditText) v.findViewById(R.id.amount);
        send = (Button) v.findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(amountInput.getText().toString());
                Log.v(this.getClass().getSimpleName(), "Inside Listener");
                if (amount > accountBalance) {
                    Snackbar.make(send, "Inadequate Balance!!!", Snackbar.LENGTH_LONG).show();
                    Log.v(this.getClass().getSimpleName(), "Inadequate amount");
                }
                String date = new Date().toString();
                date = date.substring(0, date.indexOf("GMT"));
                onTransactionSuccessful(receiverPhoneNumber, date, amount);
            }
        });
        send.setEnabled(false);
        user = getContext().getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("username", "");
        return v;
    }

    void onTransactionSuccessful (String receiver, String date_time, float amount) {
        JSONObject temp = new JSONObject();
        try {
            temp.put("receiver", receiver);
            temp.put("date_time", date_time);
            temp.put("amount", Float.toString(amount));
            JSONArray jsonArray = readJsonfromFile(historyFile);
            if(jsonArray == null)
                jsonArray = new JSONArray();
            jsonArray.put(temp);
            writeJsontoFile(jsonArray);
            Snackbar.make(send, "Transaction Complete!!!", Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    JSONArray readJsonfromFile(File historyFile) throws FileNotFoundException, JSONException {
        if(!historyFile.exists())
            try {
                historyFile.createNewFile();
                Log.v(this.getClass().getSimpleName(), "File was absent, now created");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        FileInputStream fis = new FileInputStream(historyFile);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(sb.toString().length() != 0)
            return new JSONArray(sb.toString());
        else
            return null;
    }

    public void writeJsontoFile(JSONArray jsonArray) throws IOException {
        if(jsonArray != null)
            Log.v(this.getClass().getSimpleName(), jsonArray.toString());
        FileOutputStream fos = new FileOutputStream(historyFile, false);
        fos.write(jsonArray.toString().getBytes());
        fos.close();
        Log.v(this.getClass().getSimpleName(), "Write complete");
        try {
            Log.v(this.getClass().getSimpleName(), "File after writing : " + readJsonfromFile(historyFile).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    String receiverPhoneNumber;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
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
            Log.d(TAG, "Phone Number : " + receiverPhoneNumber);
            ParseQuery query = ParseUser.getQuery();
            query.whereEqualTo("username", receiverPhoneNumber);
            final String finalPhoneNumber = receiverPhoneNumber;
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        // The query was successful.
                        // check if we got a match
                        ParseUser p = user;
                        String full_name = (String) user.get("Full_Name");
                        Log.d(TAG, "Full name of user : " + full_name);
                        int amount = Integer.parseInt(amountInput.getText().toString());
                        int receiver_account_balance = user.getInt("account_balance");
                        int sender_account_balance = ParseUser.getCurrentUser().getInt("account_balance");
                        receiver_account_balance += amount;
                        sender_account_balance -= amount;

                        String s = (String) ParseUser.getCurrentUser().get("Full_Name");

                        ParseObject transaction_record = new ParseObject("Transaction_Record");

                        transaction_record.put("Sender_Name", s);
                        transaction_record.put("Sender_Number", ParseUser.getCurrentUser().getUsername());
                        transaction_record.put("Receiver_Name", full_name);
                        transaction_record.put("Receiver_Number", receiverPhoneNumber );
                        transaction_record.put("Amount", amount);
                        transaction_record.put("Transcation_Status", "0"); //Status = 0 means transaction incomplete.


                        transaction_record.saveInBackground();

                        user.put("account_balance", receiver_account_balance);
                        user.saveInBackground();
                        p.put("account_balance", receiver_account_balance);
                        p.saveInBackground();
                        ParseUser.getCurrentUser().put("account_balance", sender_account_balance);
                        ParseUser.getCurrentUser().saveInBackground();
                        receiverInfo.setText(getResources().getString(R.string.receiver) + full_name + "\nPhone Number : " + finalPhoneNumber
                                + "\n" + getResources().getString(R.string.thanks_msg));
                        n = sender_account_balance;
                        availableBalance.setText("" + sender_account_balance);
                        if (user == null) {
                            Log.d(TAG, "No matching user. ");
                        } else {
                            full_name = (String) user.get("Full_Name");
                            user.put("account_balance", receiver_account_balance);
                            user.saveInBackground();
                            receiverInfo.setText(getResources().getString(R.string.receiver) + full_name + "\nPhone Number : " + finalPhoneNumber
                                    + "\n" + getResources().getString(R.string.thanks_msg));
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
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
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
                Picasso.with(getActivity().getApplicationContext()).load(uri).into(qrImage);
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
        Intent intent = new Intent(getActivity().getApplicationContext(),
                ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }
}
