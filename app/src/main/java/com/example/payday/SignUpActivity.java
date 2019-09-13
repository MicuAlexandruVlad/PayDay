package com.example.payday;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    private Button signUp;
    private MaterialEditText username, password, confirmPassword;

    private DBLinks dbLinks;
    private Intent parentIntent;

    public static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // get the intent that started this activity
        parentIntent = getIntent();

        // hide top bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // ui binding
        signUp = findViewById(R.id.btn_sign_up);
        username = findViewById(R.id.met_username);
        password = findViewById(R.id.met_password);
        confirmPassword = findViewById(R.id.met_confirm_password);

        dbLinks = new DBLinks();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user, pass, confPass;
                user = username.getText().toString();
                pass = password.getText().toString();
                confPass = confirmPassword.getText().toString();

                // check if data is empty
                if (user.equals("") || pass.equals("") || confPass.equals(""))
                    Toast.makeText(SignUpActivity.this, "One or " +
                            "more fields are empty", Toast.LENGTH_SHORT).show();
                else if (!pass.equals(confPass))
                    Toast.makeText(SignUpActivity.this, "Passwords do " +
                            "not match", Toast.LENGTH_SHORT).show();
                else {
                    // initialise a client and send the data to the server
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(dbLinks.getBaseLink() + "register-user?username=" + user +
                                    "&password=" + pass)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String json = response.body().string();

                            try {
                                JSONObject obj = new JSONObject(json);
                                JSONArray responseArray = obj.getJSONArray("result");

                                if (responseArray.length() != 0)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SignUpActivity.this, "User" +
                                                    " already exists.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SignUpActivity.this,
                                                    "Successfully signed up", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
}
