package com.indiahacks16.fintech.qrmoney.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.indiahacks16.fintech.qrmoney.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SendMoneyFragment extends Fragment {
    Button gallery, camera;
    TextView textQr;
    ImageView qrImage;
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
        textQr = (TextView) v.findViewById(R.id.textQr);
        qrImage = (ImageView) v.findViewById(R.id.qr_image);

        return v;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                decodeQr(selectedImageUri);
            }
            else {
                textQr.setTextColor(Color.WHITE);
                textQr.setText("QR : " +
                        data.getStringExtra(ZBarConstants.SCAN_RESULT));
                qrImage.setVisibility(View.INVISIBLE);
            }
        }
    }
    void decodeQr(Uri uri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            bitmap = null;
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(bBitmap);
                textQr.setTextColor(Color.WHITE);
                textQr.setText("QR : " + result.getText());
                Picasso.with(getActivity().getApplicationContext()).load(uri).into(qrImage);
            }
            catch (Exception e) {
                e.printStackTrace();
                textQr.setTextColor(Color.parseColor("#FFFF5248"));
                textQr.setText("QR Decoding Failed");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            textQr.setTextColor(Color.parseColor("#FFFF5248"));
            textQr.setText("QR Decoding Failed");
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
