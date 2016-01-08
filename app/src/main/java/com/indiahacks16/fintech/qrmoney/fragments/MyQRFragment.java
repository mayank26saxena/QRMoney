package com.indiahacks16.fintech.qrmoney.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.indiahacks16.fintech.qrmoney.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class MyQRFragment extends Fragment {
    ImageView mImageQr;
    ImageButton email, whatsapp, share;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_qr, container, false);
        mImageQr = (ImageView) view.findViewById(R.id.image_qr);
        SharedPreferences sp = getContext().getSharedPreferences("PHNO", Context.MODE_PRIVATE);
        String name = sp.getString("phno", "") + ".png";
        final String filePath = Environment.getExternalStorageDirectory()
                + "/qrmoney/" + name;
        Picasso.with(getContext()).load(new File(filePath)).into(mImageQr);
        email = (ImageButton) view.findViewById(R.id.email);
        whatsapp = (ImageButton) view.findViewById(R.id.whatsapp);
        share = (ImageButton) view.findViewById(R.id.share);
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
                        mailClient.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
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
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
                startActivity(sendIntent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath)); // Add image path
                startActivity(Intent.createChooser(share, "Share image using"));
            }
        });
        return view;
    }
}
