package com.indiahacks16.fintech.qrmoney.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.indiahacks16.fintech.qrmoney.Contents;
import com.indiahacks16.fintech.qrmoney.HistoryRecyclerAdapter;
import com.indiahacks16.fintech.qrmoney.QRCodeEncoder;
import com.indiahacks16.fintech.qrmoney.R;
import com.indiahacks16.fintech.qrmoney.Transaction;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyAccountFragment extends Fragment {
    ImageView mImageQr;
    ImageButton email, whatsapp, share;
    RecyclerView history;
    ArrayList<Transaction> transactionList = new ArrayList<>();
    File historyFile;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        mImageQr = (ImageView) view.findViewById(R.id.image_qr);
        SharedPreferences sp = getContext().getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        String name = sp.getString("username", "");
        Log.v(this.getClass().getSimpleName(), name);
        final String filePath = Environment.getExternalStorageDirectory()
                + "/qrmoney/" + "$$" + name + "##" + ".png";
        File file = new File(filePath);
        final Uri uri = Uri.fromFile(file);
        //Picasso.with(getContext()).load(new File(filePath)).into(mImageQr);
        name = "$$" + name + "##";
        QRCodeEncoder qrCodeEncoder =
                new QRCodeEncoder(name, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 300);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            mImageQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        email = (ImageButton) view.findViewById(R.id.email);
        whatsapp = (ImageButton) view.findViewById(R.id.whatsapp);
        share = (ImageButton) view.findViewById(R.id.share);
        historyFile = new File(Environment.getExternalStorageDirectory() + "/qrmoney/history/" +
                getContext().getSharedPreferences("LOGIN", Context.MODE_PRIVATE).getString("username", "") + ".json");
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mailClient = new Intent(Intent.ACTION_SEND);
                Intent tempIntent = new Intent(Intent.ACTION_SEND);
                tempIntent.setType("*/*");
                List<ResolveInfo> resInfo = getActivity().getPackageManager()
                        .queryIntentActivities(tempIntent, 0);
                for (int i = 0; i < resInfo.size(); i++) {
                    ResolveInfo ri = resInfo.get(i);
                    if (ri.activityInfo.packageName.contains("android.gm")){
                        mailClient.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                        mailClient.setType("message/rfc822");
                        mailClient.putExtra(Intent.EXTRA_SUBJECT, "My QR Money Code!!");
                        mailClient.putExtra(Intent.EXTRA_TEXT, "Send money in a few seconds using the QR Money App");
                        mailClient.putExtra(Intent.EXTRA_STREAM, uri);
                    }
                }
                startActivity(mailClient);
            }
        });
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "My QR Money Code");
                sendIntent.setType("image/png");
                sendIntent.setPackage("com.whatsapp");
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(sendIntent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, uri); // Add image path
                startActivity(Intent.createChooser(share, "Share image using"));
            }
        });
        history = (RecyclerView) view.findViewById(R.id.history_transaction);
        history.setLayoutManager(new LinearLayoutManager(getContext()));
        //processHistory(getContext().getSharedPreferences("PHNO", Context.MODE_PRIVATE).getString("phno", ""));
        processHistory();
        return view;
    }

    void processHistory() {
        try {
            JSONArray jsonArray = readJsonfromFile();
            if(jsonArray != null) {
                for(int i = 0 ; i < jsonArray.length() ; i++) {
                    JSONObject temp = jsonArray.getJSONObject(i);
                    Transaction transaction = new Transaction(temp.getString("receiver"),
                            temp.getString("date_time"),
                            temp.getString("amount"));
                    transactionList.add(transaction);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        history.setAdapter(new HistoryRecyclerAdapter(transactionList));
    }

    JSONArray readJsonfromFile() throws FileNotFoundException, JSONException {
        if(!historyFile.exists())
            try {
                historyFile.createNewFile();
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
}
