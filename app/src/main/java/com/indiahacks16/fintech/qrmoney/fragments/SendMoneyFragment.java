package com.indiahacks16.fintech.qrmoney.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SendMoneyFragment extends Fragment {
    Button gallery, camera, send;
    ImageView qrImage;
    TextView receiverInfo, availableBalance;
    EditText amountInput;
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
        qrImage = (ImageView) v.findViewById(R.id.qr_image);
        receiverInfo = (TextView) v.findViewById(R.id.receiver_info);
        final int accountBalance = AddMoneyFragment.accountBalance;
        availableBalance = (TextView) v.findViewById(R.id.available_balance);
        availableBalance.setText("RS. " + accountBalance);
        amountInput = (EditText) v.findViewById(R.id.amount);
        send = (Button) v.findViewById(R.id.send_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(amountInput.getText().toString());
                Log.v(this.getClass().getSimpleName(), "Inside Listener");
                if(amount > accountBalance) {
                    Snackbar.make(send, "Inadequate Balance!!!", Snackbar.LENGTH_LONG).show();
                    Log.v(this.getClass().getSimpleName(), "Inadequate amount");
                }
            }
        });
        send.setEnabled(false);
        return v;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String receiverInfo2, phoneNumber;
        if(resultCode == Activity.RESULT_OK) {
            send.setEnabled(true);
            if(requestCode == FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                phoneNumber = decodeQr(selectedImageUri);
            }
            else {
                phoneNumber = data.getStringExtra(ZBarConstants.SCAN_RESULT);
                phoneNumber = phoneNumber.substring(2, phoneNumber.length() - 2);
                qrImage.setImageBitmap(generateQR(data.getStringExtra(ZBarConstants.SCAN_RESULT)));
            }
            /**
             * Add Code to get Name from Parse using Phone Number
             */
            receiverInfo.setText("RECEIVER : \n" + phoneNumber);
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
