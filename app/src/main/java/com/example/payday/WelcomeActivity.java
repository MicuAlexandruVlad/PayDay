package com.example.payday;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    private Button signUp, login;
    private ImageView settings;

    private DBLinks dbLinks;

    public static final int CHANGE_DB_LINK_REQ_CODE = 1241;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // hide top bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // ui binding
        login = findViewById(R.id.btn_login);
        signUp = findViewById(R.id.btn_sign_up);
        settings = findViewById(R.id.iv_settings);

        dbLinks = new DBLinks();

        // listeners and start activities
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

}
