package com.example.payday;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddToBalanceActivity extends AppCompatActivity {

    private TextView balanceBefore, balanceAfter, whereTv;
    private RadioButton add;
    private MaterialEditText amount, from;
    private LinearLayout op1, balanceAfterHolder;
    private Button done;
    private LottieAnimationView success;

    private Intent parentIntent;
    private int balance;
    private User currentUser;
    private DBLinks dbLinks;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_balance);

        Objects.requireNonNull(getSupportActionBar()).hide();

        balanceBefore = findViewById(R.id.tv_balance_before);
        balanceAfter = findViewById(R.id.tv_balance_after);
        whereTv = findViewById(R.id.tv_3);
        add = findViewById(R.id.rb_add);
        done = findViewById(R.id.btn_done);
        amount = findViewById(R.id.met_amount);
        from = findViewById(R.id.met_from);
        op1 = findViewById(R.id.ll_operations_1);
        balanceAfterHolder = findViewById(R.id.ll_2);
        success = findViewById(R.id.l_success);

        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        balance = parentIntent.getIntExtra("balance", 0);
        balanceAfter.setText(balance + "");
        balanceBefore.setText(balance + "");

        amount.setVisibility(View.INVISIBLE);
        balanceAfterHolder.setVisibility(View.INVISIBLE);
        whereTv.setVisibility(View.INVISIBLE);
        from.setVisibility(View.INVISIBLE);
        success.setVisibility(View.INVISIBLE);

        dbLinks = new DBLinks();

        final AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        //fadeIn.setFillAfter(true);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                balanceAfterHolder.setVisibility(View.VISIBLE);
                amount.setVisibility(View.VISIBLE);
                from.setVisibility(View.VISIBLE);
                whereTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        op1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add.setChecked(!add.isChecked());
            }
        });

        add.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (amount.getVisibility() == View.INVISIBLE) {
                    amount.startAnimation(fadeIn);
                    balanceAfterHolder.startAnimation(fadeIn);
                    whereTv.startAnimation(fadeIn);
                    from.startAnimation(fadeIn);
                }
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals("")) {
                    if (add.isChecked()) {
                        int val = Integer.valueOf(amount.getText().toString()) + balance;
                        balanceAfter.setText(val + "");
                    }
                }
                else {
                    balanceAfter.setText(balance + "");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Operation operation = new Operation();
                int amountVal = 0;
                if (amount.getText().toString().equals("") || from.getText().toString().equals(""))
                    Toast.makeText(AddToBalanceActivity.this, "One or more" +
                            " fields are empty", Toast.LENGTH_SHORT).show();
                else {
                    success.setVisibility(View.VISIBLE);
                    success.setRepeatMode(LottieDrawable.RESTART);
                    success.playAnimation();
                    amountVal = Integer.valueOf(amount.getText().toString());
                    operation.setAmount(amountVal);
                    operation.setDate(getCurrentDate());
                    operation.setFrom(from.getText().toString());
                    operation.setReferencedUserName(currentUser.getUsername());
                    if (add.isChecked()) {
                        parentIntent.putExtra("new_balance", balance + amountVal);
                        operation.setType("add");
                    }

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(dbLinks.getBaseLink() + "add-operation?operationType=" + operation.getType() +
                                    "&operationAmount=" + operation.getAmount() + "&operationDate=" +
                                    operation.getDate() + "&operationFrom=" + operation.getFrom() +
                                    "&operationReferencedUsername=" + operation.getReferencedUserName())
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
                                JSONObject object = new JSONObject(json);
                                boolean inserted = object.getBoolean("inserted");
                                if (!inserted)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(AddToBalanceActivity.this,
                                                    "Something went wrong." +
                                                            " Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                else {
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
                                                    parentIntent.putExtra("new_operation", operation);
                                                    setResult(RESULT_OK, parentIntent);
                                                    finish();
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
    @SuppressLint("SimpleDateFormat")
    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
