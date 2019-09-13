package com.example.payday;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddNewReserveActivity extends AppCompatActivity {
    private MaterialEditText name, date,time, goalAmount, note, actualAmount;
    private LinearLayout noticeHolder;
    private CheckBox sameDayReminder, secondaryReminder, autoFill;
    private TextView sameDayReminderTV, secondaryReminderTV, lowTV, mediumTV, highTV, balanceBefore
            , balanceAfter;
    private Spinner reminder, secondaryReminderSP;
    private Button done;
    private ConstraintLayout reminderHolder;
    private RadioButton low, medium, high;

    private int year, day, month, hour, minute, priorityVal, sameDaySpinnerPos, secondarySpinnerPos;
    private boolean priorityChecked = false;
    private String sameDayReminderValue, secondaryReminderValue, symbol;
    private User currentUser;
    private Intent parentIntent;
    private DBLinks dbLinks;
    private boolean isEdit;
    private Reserve reserveToEdit;
    private int pos;

    public static final String TAG = "AddNewReserve";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reserve);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // ui binding
        name = findViewById(R.id.met_reserve_name);
        goalAmount = findViewById(R.id.met_reserve_amount);
        date = findViewById(R.id.met_reserve_date);
        time = findViewById(R.id.met_reserve_time);
        note = findViewById(R.id.met_reserve_note);
        actualAmount = findViewById(R.id.met_reserve_actual_amount);
        noticeHolder = findViewById(R.id.ll_notice_holder);
        sameDayReminder = findViewById(R.id.cb_same_day_reminder);
        secondaryReminder = findViewById(R.id.cb_secondary_reminder);
        autoFill = findViewById(R.id.cb_auto_fill);
        sameDayReminderTV = findViewById(R.id.tv6);
        secondaryReminderTV = findViewById(R.id.tv8);
        lowTV = findViewById(R.id.tv_priority_low);
        mediumTV = findViewById(R.id.tv_priority_medium);
        highTV = findViewById(R.id.tv_priority_high);
        balanceBefore = findViewById(R.id.tv_balance_before);
        balanceAfter = findViewById(R.id.tv_balance_after);
        reminder = findViewById(R.id.sp_reminder_period);
        secondaryReminderSP = findViewById(R.id.sp_secondary_reminder_period);
        done = findViewById(R.id.btn_done);
        reminderHolder = findViewById(R.id.cl_reminder_holder);
        low = findViewById(R.id.rb_priority_low);
        medium = findViewById(R.id.rb_priority_medium);
        high = findViewById(R.id.rb_priority_high);

        noticeHolder.setVisibility(View.VISIBLE);
        reminder.setVisibility(View.INVISIBLE);
        secondaryReminderSP.setVisibility(View.INVISIBLE);

        // get starter intent
        parentIntent = getIntent();
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        isEdit = parentIntent.getBooleanExtra("isEdit", false);
        if (isEdit) {
            // if this activity is used to edit a reserve, then get the necessary data from parent activity
            pos = parentIntent.getIntExtra("pos", 0);
            reserveToEdit = (Reserve) parentIntent.getSerializableExtra("reserve");
        }

        balanceBefore.setText(currentUser.getSymbol() + " " + currentUser.getBalance());

        dbLinks = new DBLinks();

        final List<String> timeList = new ArrayList<>();
        timeList.add("5 min before");
        timeList.add("15 min before");
        timeList.add("30 min before");
        timeList.add("1 hour before");
        timeList.add("2 hours before");
        timeList.add("3 hours before");
        timeList.add("4 hours before");
        timeList.add("5 hours before");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.currency_spinner_item, timeList);
        reminder.setAdapter(adapter);

        final List<String> secondaryTimeList = new ArrayList<>();
        secondaryTimeList.add("1 day before");
        secondaryTimeList.add("2 days before");
        secondaryTimeList.add("3 days before");
        secondaryTimeList.add("4 days before");
        secondaryTimeList.add("5 days before");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                R.layout.currency_spinner_item, secondaryTimeList);
        secondaryReminderSP.setAdapter(adapter1);

        if (isEdit) {
            // edit an existing reserve
            name.setText(reserveToEdit.getName());
            note.setText(reserveToEdit.getNote());
            date.setText(reserveToEdit.getFullScheduledDate().split(" ")[0]);
            time.setText(reserveToEdit.getFullScheduledDate().split(" ")[1]);
            goalAmount.setText(reserveToEdit.getGoalAmount() + "");
            actualAmount.setText(reserveToEdit.getActualAmount() + "");
            if (reserveToEdit.hasSecondaryReminder()) {
                secondaryReminder.setChecked(true);
                secondaryReminderSP.setVisibility(View.VISIBLE);
                for (int i = 0; i < secondaryTimeList.size(); i++) {
                    int amount = Integer.valueOf(timeList.get(i).split(" ")[0]);
                    if (subtractReminderDays(reserveToEdit.getFullScheduledDate(), amount)
                            .equalsIgnoreCase(reserveToEdit.getSecondaryReminderDate())) {
                        secondaryReminderSP.setSelection(i);
                        secondarySpinnerPos = i;
                        break;
                    }
                }
            }
            else
                secondaryReminder.setChecked(false);
            if (reserveToEdit.hasReminderForScheduledDate()) {
                sameDayReminder.setChecked(true);
                reminder.setVisibility(View.VISIBLE);
                boolean found = false;
                for (int i = 0; i < 3; i++) {
                    int amount = Integer.valueOf(timeList.get(i).split(" ")[0]);
                    if (subtractReminderMinutes(reserveToEdit.getFullScheduledDate(), amount)
                            .equalsIgnoreCase(reserveToEdit.getSameDayReminderDate())) {
                        reminder.setSelection(i);
                        sameDaySpinnerPos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (int i = 3; i < timeList.size(); i++) {
                        int amount = Integer.valueOf(timeList.get(i).split(" ")[0]);
                        if (subtractReminderHours(reserveToEdit.getFullScheduledDate(), amount)
                                .equalsIgnoreCase(reserveToEdit.getSameDayReminderDate())) {
                            reminder.setSelection(i);
                            break;
                        }
                    }
                }
            }
            else
                sameDayReminder.setChecked(false);
            if (reserveToEdit.getPriority() == 0) {
                low.setChecked(true);
                medium.setChecked(false);
                high.setChecked(false);
            }
            else if (reserveToEdit.getPriority() == 1) {
                low.setChecked(false);
                medium.setChecked(true);
                high.setChecked(false);
            }
            else {
                low.setChecked(false);
                medium.setChecked(false);
                high.setChecked(true);
            }
            int dif = currentUser.getBalance() - reserveToEdit.getActualAmount();
            balanceAfter.setText(dif + "");
            balanceBefore.setText(currentUser.getBalance() + "");
        }

        reminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sameDayReminderValue = timeList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        secondaryReminderSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                secondaryReminderValue = secondaryTimeList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sameDayReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b && reminder.getVisibility() == View.INVISIBLE) {
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    fadeIn.setDuration(400);
                    fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            reminder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    reminder.startAnimation(fadeIn);
                }
            }
        });

        autoFill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    int balance = currentUser.getBalance();
                    if (goalAmount.getText().toString().equalsIgnoreCase("")) {
                        autoFill.setChecked(false);
                        Toast.makeText(AddNewReserveActivity.this, "Please fill " +
                                "the goal amount field first", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        int goalAmountVal = Integer.valueOf(goalAmount.getText().toString());
                        if (balance >= goalAmountVal) {
                            actualAmount.setText(goalAmountVal + "");
                            int after = balance - goalAmountVal;
                            balanceAfter.setText(currentUser.getSymbol() + " " + after);
                        }
                    }
                }
            }
        });

        actualAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equalsIgnoreCase("")) {
                    if (Integer.valueOf(charSequence.toString()) > currentUser.getBalance()) {
                        actualAmount.setText(currentUser.getBalance() + "");
                        balanceAfter.setText(currentUser.getSymbol() + " " + 0);
                    }
                    else {
                        int dif = currentUser.getBalance() - Integer.valueOf(charSequence.toString());
                        balanceAfter.setText(currentUser.getSymbol() + " " + dif);
                    }
                }
                else
                    balanceAfter.setText(currentUser.getSymbol() + " " + currentUser.getBalance());
                if (!actualAmount.getText().toString().equalsIgnoreCase(goalAmount.getText().toString())
                    && autoFill.isChecked())
                    autoFill.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        secondaryReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && secondaryReminderSP.getVisibility() == View.INVISIBLE) {
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    fadeIn.setDuration(400);
                    fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            secondaryReminderSP.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    secondaryReminderSP.startAnimation(fadeIn);
                }
            }
        });

        lowTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                low.setChecked(true);
                medium.setChecked(false);
                high.setChecked(false);

                priorityChecked = true;
                priorityVal = 0;
            }
        });

        mediumTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                low.setChecked(false);
                medium.setChecked(true);
                high.setChecked(false);

                priorityChecked = true;
                priorityVal = 1;
            }
        });

        highTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                low.setChecked(false);
                medium.setChecked(false);
                high.setChecked(true);

                priorityChecked = true;
                priorityVal = 2;
            }
        });

        low.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    medium.setChecked(false);
                    high.setChecked(false);

                    priorityChecked = true;
                    priorityVal = 0;
                }
            }
        });

        medium.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    low.setChecked(false);
                    high.setChecked(false);

                    priorityChecked = true;
                    priorityVal = 1;
                }
            }
        });

        high.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    low.setChecked(false);
                    medium.setChecked(false);

                    priorityChecked = true;
                    priorityVal = 2;
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog  datePickerDialog = new DatePickerDialog(AddNewReserveActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    public void onDateSet(DatePicker view, int y, int monthOfYear, int dayOfMonth) {
                        String m, d;
                        if (monthOfYear < 10)
                            m = "0" + monthOfYear;
                        else
                            m = monthOfYear + "";
                        if (dayOfMonth < 10)
                            d = "0" + dayOfMonth;
                        else
                            d = dayOfMonth + "";
                        date.setText(m + "/" + d + "/" + y);
                        day = dayOfMonth;
                        year = y;
                        month = monthOfYear;
                    }

                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.create();
                datePickerDialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentTime = Calendar.getInstance();
                int h = currentTime.get(Calendar.HOUR_OF_DAY);
                int m = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddNewReserveActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                if (i < 10 && i1 < 10)
                                    time.setText("0" + i + ":" + "0" + i1);
                                if (i < 10 && i1 >= 10)
                                    time.setText("0" + i + ":" + i1);
                                if (i >= 10 && i1 < 10)
                                    time.setText(i + ":" + "0" + i1);
                                if (i >= 10 && i1 >= 10)
                                    time.setText(i + ":" + i1);
                                hour = i;
                                minute = i1;
                            }
                        }, h, m, true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("SimpleDateFormat")
            public void onClick(View view) {

                String nameVal, amountVal, dateVal, timeVal, noteVal;
                nameVal = name.getText().toString();
                amountVal = goalAmount.getText().toString();
                dateVal = date.getText().toString();
                timeVal = time.getText().toString();
                noteVal = note.getText().toString();

                if (nameVal.equalsIgnoreCase("") || amountVal.equalsIgnoreCase("")
                    || dateVal.equalsIgnoreCase("") || timeVal.equalsIgnoreCase(""))
                    Toast.makeText(AddNewReserveActivity.this,
                            "One or more fields are empty", Toast.LENGTH_SHORT).show();
                else if (!isEdit)
                        if (!priorityChecked)
                            Toast.makeText(AddNewReserveActivity.this,
                                    "Priority is not checked", Toast.LENGTH_SHORT).show();
                    else {
                        final Reserve reserve = new Reserve();
                        reserve.setName(nameVal);
                        reserve.setGoalAmount(Integer.valueOf(amountVal));
                        reserve.setSubtractionTime(timeVal);
                        reserve.setNote(noteVal);
                        reserve.setPriority(priorityVal);
                        reserve.setFinalised(false);
                        if (actualAmount.getText().toString().equalsIgnoreCase(""))
                            reserve.setActualAmount(0);
                        else
                            reserve.setActualAmount(Integer.valueOf(actualAmount.getText().toString()));
                        reserve.setFullScheduledDate(date.getText().toString() + " " + time.getText().toString());
                        if (sameDayReminder.isChecked()) {
                            reserve.setReminderForScheduledDate(true);
                            String fullDate = dateVal + " " + timeVal;
                            if (sameDayReminderValue.split(" ")[1].equalsIgnoreCase("min")) {
                                int amount = Integer.valueOf(sameDayReminderValue.split(" ")[0]);
                                reserve.setSameDayReminderDate(subtractReminderMinutes(fullDate, amount));
                            }
                            if (sameDayReminderValue.split(" ")[1].equalsIgnoreCase("hour")
                                || sameDayReminderValue.split(" ")[1].equalsIgnoreCase("hours")) {
                                int amount = Integer.valueOf(sameDayReminderValue.split(" ")[0]);
                                reserve.setSameDayReminderDate(subtractReminderHours(fullDate, amount));
                            }
                        }
                        else {
                            reserve.setReminderForScheduledDate(false);
                            reserve.setSameDayReminderDate("");
                        }
                        if (secondaryReminder.isChecked()) {
                            reserve.setSecondaryReminder(true);
                            String fullDate = dateVal + " " + timeVal;
                            int amount = Integer.valueOf(secondaryReminderValue.split(" ")[0]);
                            reserve.setSecondaryReminderDate(subtractReminderDays(fullDate, amount));
                        }
                        else {
                            reserve.setSecondaryReminder(false);
                            reserve.setSecondaryReminderDate("");
                        }

                        // TODO: upload reserve to database - done
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(dbLinks.getBaseLink() + "register-reserve?reserveName="
                                + reserve.getName() + "&note=" + reserve.getNote()
                                + "&reminderForScheduledDate=" + reserve.hasReminderForScheduledDate()
                                + "&secondaryReminder=" + reserve.hasSecondaryReminder()
                                + "&sameDayReminderDate=" + reserve.getSameDayReminderDate()
                                + "&fullScheduledDate=" + reserve.getFullScheduledDate()
                                + "&referencedUsername=" + currentUser.getUsername()
                                + "&subtractionTime=" + reserve.getSubtractionTime()
                                + "&goalAmount=" + reserve.getGoalAmount()
                                + "&actualAmount=" + reserve.getActualAmount()
                                + "&secondaryReminderDate=" + reserve.getSecondaryReminderDate()
                                + "&priority=" + reserve.getPriority()
                                + "&finalised=" + reserve.isFinalised())
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
                                    if (inserted) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AddNewReserveActivity.this)
                                                        .setMessage("Reserve has been successfully saved to database.")
                                                        .setTitle("Success")
                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                                // TODO: finish activity and send new reserve back to parent activity - done
                                                                parentIntent.putExtra("new_reserve", reserve);
                                                                setResult(RESULT_OK, parentIntent);
                                                                finish();
                                                            }
                                                        });
                                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialogInterface) {
                                                        parentIntent.putExtra("new_reserve", reserve);
                                                        setResult(RESULT_OK, parentIntent);
                                                        finish();
                                                    }
                                                });
                                                builder.create();
                                                builder.show();
                                            }
                                        });
                                    }
                                    else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AddNewReserveActivity.this)
                                                        .setMessage("Something went wrong. Please try again later.")
                                                        .setTitle("Oops")
                                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        });
                                                builder.create();
                                                builder.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {
                        //TODO: do stuff for when data is edited;
                        if (reserveToEdit.getActualAmount() == Integer.valueOf(actualAmount.getText().toString())
                            && reserveToEdit.getPriority() == priorityVal
                                && reserveToEdit.getName().equals(nameVal)
                                && reserveToEdit.getFullScheduledDate().equalsIgnoreCase(date.getText().toString() + " "
                                + time.getText().toString()) && reserveToEdit.hasReminderForScheduledDate() == sameDayReminder.isChecked()
                                && reserveToEdit.hasSecondaryReminder() == secondaryReminder.isChecked()
                                && reminder.getSelectedItemPosition() == sameDaySpinnerPos
                                && secondaryReminderSP.getSelectedItemPosition() == secondarySpinnerPos
                                && reserveToEdit.getGoalAmount() == Integer.valueOf(goalAmount.getText().toString())
                                && reserveToEdit.getNote().equals(note.getText().toString())
                                && isSecondaryReminderDateDifferent(date.getText().toString() + " "
                                + time.getText().toString(), secondaryTimeList)
                                && isSameDayReminderDateDifferent(date.getText().toString() + " "
                                + time.getText().toString(), timeList))
                            Toast.makeText(AddNewReserveActivity.this,
                                    "Please change the data", Toast.LENGTH_SHORT).show();
                        else {
                            // TODO: update entry in database and send new reserve back to parent activity - done
                            // TODO: create path in API to allow entry update - done
                            if (low.isChecked())
                                reserveToEdit.setPriority(0);
                            if (medium.isChecked())
                                reserveToEdit.setPriority(1);
                            if (high.isChecked())
                                reserveToEdit.setPriority(2);
                            reserveToEdit.setName(name.getText().toString());
                            reserveToEdit.setNote(note.getText().toString());
                            reserveToEdit.setFinalised(false);
                            reserveToEdit.setReferencedUsername(currentUser.getUsername());
                            reserveToEdit.setGoalAmount(Integer.valueOf(goalAmount.getText().toString()));
                            if (actualAmount.getText().toString().equalsIgnoreCase(""))
                                reserveToEdit.setActualAmount(0);
                            else
                                reserveToEdit.setActualAmount(Integer.valueOf(actualAmount.getText().toString()));
                            reserveToEdit.setSubtractionTime(time.getText().toString());
                            reserveToEdit.setFullScheduledDate(date.getText().toString() + " "
                                + time.getText().toString());
                            if (sameDayReminder.isChecked()) {
                                reserveToEdit.setReminderForScheduledDate(true);
                                String fullDate = dateVal + " " + timeVal;
                                if (sameDayReminderValue.split(" ")[1].equalsIgnoreCase("min")) {
                                    int amount = Integer.valueOf(sameDayReminderValue.split(" ")[0]);
                                    reserveToEdit.setSameDayReminderDate(subtractReminderMinutes(fullDate, amount));
                                }
                                if (sameDayReminderValue.split(" ")[1].equalsIgnoreCase("hour")
                                        || sameDayReminderValue.split(" ")[1].equalsIgnoreCase("hours")) {
                                    int amount = Integer.valueOf(sameDayReminderValue.split(" ")[0]);
                                    reserveToEdit.setSameDayReminderDate(subtractReminderHours(fullDate, amount));
                                }
                            }
                            else {
                                reserveToEdit.setReminderForScheduledDate(false);
                                reserveToEdit.setSameDayReminderDate("");
                            }
                            if (secondaryReminder.isChecked()) {
                                reserveToEdit.setSecondaryReminder(true);
                                String fullDate = dateVal + " " + timeVal;
                                int amount = Integer.valueOf(secondaryReminderValue.split(" ")[0]);
                                reserveToEdit.setSecondaryReminderDate(subtractReminderDays(fullDate, amount));
                            }
                            else {
                                reserveToEdit.setSecondaryReminder(false);
                                reserveToEdit.setSecondaryReminderDate("");
                            }



                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(dbLinks.getBaseLink() + "update-reserve?reserveName="
                                            + reserveToEdit.getName() + "&note=" + reserveToEdit.getNote()
                                            + "&reminderForScheduledDate=" + reserveToEdit.hasReminderForScheduledDate()
                                            + "&secondaryReminder=" + reserveToEdit.hasSecondaryReminder()
                                            + "&sameDayReminderDate=" + reserveToEdit.getSameDayReminderDate()
                                            + "&fullScheduledDate=" + reserveToEdit.getFullScheduledDate()
                                            + "&referencedUsername=" + currentUser.getUsername()
                                            + "&subtractionTime=" + reserveToEdit.getSubtractionTime()
                                            + "&goalAmount=" + reserveToEdit.getGoalAmount()
                                            + "&actualAmount=" + reserveToEdit.getActualAmount()
                                            + "&secondaryReminderDate=" + reserveToEdit.getSecondaryReminderDate()
                                            + "&priority=" + reserveToEdit.getPriority()
                                            + "&finalised=" + reserveToEdit.isFinalised())
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
                                        JSONObject res = new JSONObject(json);
                                        boolean updated = res.getBoolean("updated");

                                        if (updated) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AddNewReserveActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            parentIntent.putExtra("pos", pos);
                                            parentIntent.putExtra("isEdit", isEdit);
                                            parentIntent.putExtra("edited_reserve", reserveToEdit);
                                            setResult(RESULT_OK, parentIntent);
                                            finish();
                                        }
                                        else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AddNewReserveActivity.this, "Failure", Toast.LENGTH_SHORT).show();
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
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String subtractReminderMinutes(String date, int amount) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        Date date1 = new Date();
        try {
            date1 = format.parse(date);
            calendar.setTime(date1);

            calendar.add(Calendar.MINUTE, 0 - amount);
            Log.d(TAG, "onClick: subtracted date -> " + calendar.getTime());
            return format.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    private String subtractReminderHours(String date, int amount) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        Date date1 = new Date();
        try {
            date1 = format.parse(date);
            calendar.setTime(date1);

            calendar.add(Calendar.HOUR, 0 - amount);
            Log.d(TAG, "onClick: subtracted date -> " + calendar.getTime());
            return format.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    @SuppressLint("SimpleDateFormat")
    private String subtractReminderDays(String date, int amount) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        Date date1 = new Date();
        try {
            date1 = format.parse(date);
            calendar.setTime(date1);

            calendar.add(Calendar.DATE, 0 - amount);
            Log.d(TAG, "onClick: subtracted date -> " + calendar.getTime());
            return format.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    private boolean isSecondaryReminderDateDifferent(String fullDate, List<String> timeList) {
        if (reserveToEdit.getSecondaryReminderDate().equalsIgnoreCase(subtractReminderDays(fullDate
                , Integer.valueOf(timeList.get(secondaryReminderSP.getSelectedItemPosition()).split(" ")[0]))))
            return false;
        return true;
    }

    private boolean isSameDayReminderDateDifferent(String fullDate, List<String> timeList) {
        String original = reserveToEdit.getSameDayReminderDate();
        String reminderDate;

        if (timeList.get(reminder.getSelectedItemPosition()).split(" ")[1].equalsIgnoreCase("min"))
            reminderDate = subtractReminderMinutes(fullDate
                    , Integer.valueOf(timeList.get(reminder.getSelectedItemPosition()).split(" ")[0]));
        else
            reminderDate = subtractReminderHours(fullDate
                    , Integer.valueOf(timeList.get(reminder.getSelectedItemPosition()).split(" ")[0]));
        if (original.equalsIgnoreCase(reminderDate))
            return false;
        return true;
    }
}
