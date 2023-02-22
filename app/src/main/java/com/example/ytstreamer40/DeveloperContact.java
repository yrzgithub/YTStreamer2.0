package com.example.ytstreamer40;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class DeveloperContact extends AppCompatActivity implements View.OnClickListener {

    LinearLayout instagram,facebook,linkedin,youtube,gmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_contact2);

        instagram = findViewById(R.id.instagram);
        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linked_in);
        youtube = findViewById(R.id.youtube);
        gmail = findViewById(R.id.gmail);

        instagram.setOnClickListener(this);
        facebook.setOnClickListener(this);
        linkedin.setOnClickListener(this);
        youtube.setOnClickListener(this);
        gmail.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.instagram:
                startActivity(MainActivity.browse("https://www.instagram.com/alpha_yr"));
                break;

            case R.id.facebook:
                startActivity(MainActivity.browse("https://www.facebook.com/y.r.kumar.1232/"));
                break;

            case R.id.linked_in:
                startActivity(MainActivity.browse("https://www.linkedin.com/in/sanjay-kumar-y-r-6a88b6207/"));
                break;

            case R.id.youtube:
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Select Channel")
                        .setPositiveButton("Sanjay kumar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(MainActivity.browse("https://www.youtube.com/channel/UC6wZDLRN5RPimxqIdoR6g_g"));
                            }
                        })
                        .setNegativeButton("Developers Inside", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(MainActivity.browse("https://www.youtube.com/channel/UCPOkSZ7GGwgVjVQqP2MjviA"));
                            }
                        }).show();
                break;

            case R.id.gmail:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("mailto:seenusanjay20102002@gmail.com"));
                startActivity(intent);
                break;
        }
    }
}