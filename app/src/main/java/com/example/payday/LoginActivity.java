package com.example.payday;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private MaterialEditText username, password;
    private Button login;

    private DBLinks dbLinks;
    private List<Operation> operations;
    private LottieAnimationView success;
    private List<Reserve> finalisedReserves, unfinalisedReserves;
    private String currentDate;
    private Intent parentIntent;

    public static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // remove top bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // get intent from previous activity
        parentIntent = getIntent();

        // ui binding
        login = findViewById(R.id.btn_login);
        username = findViewById(R.id.met_username);
        password = findViewById(R.id.met_password);
        success = findViewById(R.id.l_success);

        success.setVisibility(View.INVISIBLE);

        currentDate = getFullCurrentDate();

        username.setText("test");
        password.setText("test");

        operations = new ArrayList<>();
        finalisedReserves = new ArrayList<>();
        unfinalisedReserves = new ArrayList<>();
        dbLinks = new DBLinks();

        Log.d(TAG, "onCreate: db link -> " + dbLinks.getBaseLink());

        success.setAnimation("loading.json");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user, pass;
                // get data from ui fields
                user = username.getText().toString();
                pass = password.getText().toString();

                if (user.equals("") || pass.equals(""))
                    Toast.makeText(LoginActivity.this, "One or more" +
                            " fields are empty", Toast.LENGTH_SHORT).show();
                else {
                    // fields are not empty
                    success.setVisibility(View.VISIBLE);
                    success.setRepeatMode(LottieDrawable.RESTART);
                    success.playAnimation();
                    // initialise client and send username and password as url params
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(dbLinks.getBaseLink() + "user-login?username=" + user +
                                    "&password=" + pass)
                            .build();
                    Call call = client.newCall(request);
                    // perform get req
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            // server responded
                            String json = response.body().string();

                            try {
                                JSONArray array = new JSONArray(json);
                                if (array.length() == 0)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // get request has returned nothing
                                            success.setVisibility(View.INVISIBLE);
                                            Toast.makeText(LoginActivity.this, "User" +
                                                    " not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                else {
                                    // user exists
                                    Log.d(TAG, "onResponse: " + array.toString());
                                    // initialise an user object with the received data
                                    User user = new User();
                                    user.setPassword(array.getJSONObject(0).getString("password"));
                                    user.setUsername(array.getJSONObject(0).getString("username"));
                                    final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("currentUser", user);
                                    intent.putExtra("db_links", dbLinks);
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .url(dbLinks.getBaseLink() + "data-for-user"
                                                    + "?referencedUsername=" + user.getUsername())
                                            .build();
                                    // request all of the data associated with the current user
                                    Call call1 = client.newCall(request);
                                    call1.enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            String json = response.body().string();

                                            try {
                                                JSONObject res = new JSONObject(json);
                                                JSONArray resOperations = res.getJSONArray("operations");
                                                JSONArray resReserves = res.getJSONArray("reserves");
                                                for (int i = 0; i < resOperations.length(); i++) {
                                                    Operation operation = new Operation();
                                                    JSONObject entry = resOperations.getJSONObject(i);
                                                    operation.setAmount(entry.getInt("operationAmount"));
                                                    operation.setDate(entry.getString("operationDate"));
                                                    operation.setFrom(entry.getString("operationFrom"));
                                                    operation.setType(entry.getString("operationType"));
                                                    operation.setReferencedUserName(entry.getString("operationReferencedUsername"));
                                                    operations.add(operation);
                                                }
                                                //TODO: change this as you change reserve obj - done
                                                for (int i = 0; i < resReserves.length(); i++) {
                                                    Reserve reserve = new Reserve();
                                                    JSONObject entry = resReserves.getJSONObject(i);
                                                    reserve.setName(entry.getString("reserveName"));
                                                    reserve.setReferencedUsername(entry.getString("referencedUsername"));
                                                    reserve.setSameDayReminderDate(entry.getString("sameDayReminderDate"));
                                                    reserve.setNote(entry.getString("note"));
                                                    reserve.setReminderForScheduledDate(entry.getBoolean("reminderForScheduledDate"));
                                                    reserve.setSecondaryReminder(entry.getBoolean("secondaryReminder"));
                                                    reserve.setFullScheduledDate(entry.getString("fullScheduledDate"));
                                                    reserve.setSubtractionTime(entry.getString("subtractionTime"));
                                                    reserve.setGoalAmount(entry.getInt("goalAmount"));
                                                    reserve.setActualAmount(entry.getInt("actualAmount"));
                                                    reserve.setSecondaryReminderDate(entry.getString("secondaryReminderDate"));
                                                    reserve.setPriority(entry.getInt("priority"));
                                                    reserve.setFinalised(entry.getBoolean("finalised"));

                                                    if (dateAfter(currentDate, reserve.getFullScheduledDate())) {
                                                        reserve.setFinalised(true);
                                                        finalisedReserves.add(reserve);
                                                    }
                                                    else {
                                                        reserve.setFinalised(false);
                                                        unfinalisedReserves.add(reserve);
                                                    }
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        success.clearAnimation();
                                                        success.setAnimation("success.json");
                                                        success.setRepeatCount(0);
                                                        success.playAnimation();
                                                        success.addAnimatorListener(new Animator.AnimatorListener() {
                                                            @Override
                                                            public void onAnimationStart(Animator animator) {

                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animator animator) {
                                                                // stop animation and start the main activity
                                                                success.clearAnimation();
                                                                success.setVisibility(View.INVISIBLE);
                                                                // send all of the data from the server through an intent to next activity
                                                                intent.putExtra("operations", (Serializable) operations);
                                                                intent.putExtra("finalised_reserves", (Serializable) finalisedReserves);
                                                                intent.putExtra("unfinalised_reserves", (Serializable) unfinalisedReserves);
                                                                startActivity(intent);
                                                            }

                                                            @Override
                                                            public void onAnimationCancel(Animator animator) {

                                                            }

                                                            @Override
                                                            public void onAnimationRepeat(Animator animator) {

                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
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

    // checks whether a date follows another one or not
    @SuppressLint("SimpleDateFormat")
    private boolean dateAfter(String date1, String date2) {

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            Date d1 = format.parse(date1);
            Date d2 = format.parse(date2);
            Log.d(TAG, "dateAfter: d1 -> " + d1.toString());
            Log.d(TAG, "dateAfter: d2 -> " + d2.toString());

            if (d1.after(d2)) {
                Log.d(TAG, "dateAfter: after -> true");
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "dateAfter: after -> false");
        return false;
    }

    // returns current device date as string
    @SuppressLint("SimpleDateFormat")
    private String getFullCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
