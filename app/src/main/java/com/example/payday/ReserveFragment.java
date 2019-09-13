package com.example.payday;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;

public class ReserveFragment extends Fragment {
    private ImageView swipe;
    private LinearLayout noReserves;
    private FloatingActionButton addReserve;
    private ExpandableHeightListView reservesList;

    private User currentUser;
    private List<Operation> operations;
    private List<Reserve> reserves;
    private ViewPager pager;
    private MainPagerAdapter adapter;
    private String symbol;
    private ReserveAdapter reserveAdapter;
    private DBLinks dbLinks;
    private List<PendingIntent> pendingIntents;

    SendReserves SR;

    public static final String TAG = "ReserveFrag";

    public ReserveFragment() {
        // Required empty public constructor
    }

    public static ReserveFragment newInstance(int page, String title, Bundle bundleData, User user) {
        ReserveFragment fragment = new ReserveFragment();
        bundleData.putSerializable("currentUser", user);
        fragment.setArguments(bundleData);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
            operations = (List<Operation>) getArguments().getSerializable("operations");
            reserves = new ArrayList<>();
            dbLinks = new DBLinks();
            reserves = (List<Reserve>) getArguments().getSerializable("unfinalised_reserves");
        }
    }

    // TODO: if reserve is finalised add it to expenses

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reserve, container, false);

        swipe = view.findViewById(R.id.iv_swipe);
        addReserve = view.findViewById(R.id.fab_add_reserve);
        noReserves = view.findViewById(R.id.ll_no_reserves);
        reservesList = view.findViewById(R.id.ehl_reserves);
        pager = Objects.requireNonNull(getActivity()).findViewById(R.id.viewpager);
        adapter = (MainPagerAdapter) pager.getAdapter();

        assert adapter != null;
        currentUser = adapter.getCurrentUser();
        symbol = adapter.getSymbol();
        dbLinks = new DBLinks();
        pendingIntents = new ArrayList<>();

        reserveAdapter = new ReserveAdapter(getContext(), reserves, symbol, true);
        reservesList.setAdapter(reserveAdapter);
        reservesList.setExpanded(true);
        reservesList.setDividerHeight(0);
        reservesList.setDivider(null);

        createPendingIntents();

        Log.d(TAG, "onCreateView: symbol -> " + symbol);

        if (reserves.size() == 0) {
            noReserves.setVisibility(View.VISIBLE);
            reservesList.setVisibility(View.GONE);
        }
        else {
            noReserves.setVisibility(View.GONE);
            reservesList.setVisibility(View.VISIBLE);
        }

        reservesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int pos = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle(reserves.get(i).getName())
                        .setMessage("Please choose one of the actions below.")
                        .setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getContext(), AddNewReserveActivity.class);
                                intent.putExtra("isEdit", true);
                                intent.putExtra("pos", pos);
                                intent.putExtra("db_links", dbLinks);
                                intent.putExtra("currentUser", currentUser);
                                intent.putExtra("reserve", reserves.get(pos));
                                startActivityForResult(intent, 2);
                            }
                        })
                        .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = reserves.get(pos).getName();
                                //reserves.remove(i);
                                final Reserve reserve = reserves.get(pos);
                                reserveAdapter.notifyDataSetChanged();
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(dbLinks.getBaseLink() + "remove-reserve"
                                                + "?reserveName=" + reserve.getName()
                                        + "&goalAmount=" + reserve.getGoalAmount()
                                        + "&actualAmount=" + reserve.getActualAmount()
                                        + "&sameDayReminderDate=" + reserve.getSameDayReminderDate())
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
                                            boolean removed = res.getBoolean("removed");
                                            if (!removed)
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), "Error." +
                                                                " Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            else {
                                                reserves.remove(pos);
                                                deleteReminders(reserve, pos);
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        SR.sendData(reserves);
                                                        Toast.makeText(getContext(), "Reserve " +
                                                               reserve.getName() + " has been removed" , Toast.LENGTH_SHORT).show();
                                                        reserveAdapter.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                builder.create();
                builder.show();

                return false;
            }
        });

        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(0);
            }
        });

        addReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddNewReserveActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra("symbol", symbol);
                intent.putExtra("isEdit", false);
                startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    private void deleteReminders(Reserve reserve, int pos) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (reserve.hasReminderForScheduledDate()) {
            PendingIntent pendingIntent = pendingIntents.get(pos);
            pendingIntents.remove(pos);
            alarmManager.cancel(pendingIntent);
        }

        if (reserve.hasSecondaryReminder()) {
            Intent myIntent = new Intent(getContext() , AlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext()
                    , pos * 100 + pos, myIntent, 0);
            alarmManager.cancel(pendingIntent);
        }
    }

    private void createPendingIntents() {
        final Intent myIntent = new Intent(getContext() , AlarmReceiver.class);
        for (int i = 0; i < reserves.size(); i++) {
            Reserve reserve = reserves.get(i);
            if (reserve.hasReminderForScheduledDate()) {
                PendingIntent p = PendingIntent.getBroadcast(getContext(), i, myIntent, 0);
                pendingIntents.add(p);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Reserve reserve = (Reserve) data.getSerializableExtra("new_reserve");
                reserves.add(reserve);
                int reservePos = reserves.size() - 1;
                setReminder(reserve, reservePos);
                SR.sendData(reserves);
                reserveAdapter.notifyDataSetChanged();
                // TODO: create new alarm for the newly created reserve - done
                if (noReserves.getVisibility() == View.VISIBLE) {
                    noReserves.setVisibility(View.GONE);
                    reservesList.setVisibility(View.VISIBLE);
                }
            }
            if (requestCode == 2) {
                boolean isEdit = data.getBooleanExtra("isEdit", false);
                int pos = data.getIntExtra("pos", 0);
                Reserve editedReserve = (Reserve) data.getSerializableExtra("edited_reserve");
                reserves.set(pos, editedReserve);
                deleteReminders(editedReserve, pos);
                setReminder(editedReserve, pos);
                SR.sendData(reserves);
                reserveAdapter.notifyDataSetChanged();
                // TODO: check if date for reminder was modified and create a new alarm - done
                // TODO: or just don't check; delete previous alarm and create it again - done
            }
        }
    }

    private void setReminder(Reserve reserve, int reservePos) {
        Intent myIntent = new Intent(getContext() , AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        myIntent.setAction("send.reserve");
        myIntent.putExtra("reserve_name", reserve.getName());
        myIntent.putExtra("reserve_goal_amount", reserve.getGoalAmount());
        myIntent.putExtra("reserve_actual_amount", reserve.getActualAmount());
        myIntent.putExtra("at", reserve.getSubtractionTime());
        myIntent.putExtra("same_day_reminder_date", reserve.getSameDayReminderDate());
        myIntent.putExtra("full_scheduled_date", reserve.getFullScheduledDate());
        myIntent.putExtra("referenced_user_name", reserve.getReferencedUsername());
        myIntent.putExtra("has_same_day_reminder", reserve.hasReminderForScheduledDate());
        myIntent.putExtra("has_secondary_reminder", reserve.hasSecondaryReminder());
        myIntent.putExtra("is_finalised", reserve.isFinalised());
        myIntent.putExtra("secondary_reminder_date", reserve.getSecondaryReminderDate());
        myIntent.putExtra("note", reserve.getNote());
        myIntent.putExtra("priority", reserve.getPriority());

        if (reserve.hasReminderForScheduledDate()) {
            String sameDayReminderDate = reserve.getSameDayReminderDate();
            int year = Integer.valueOf(sameDayReminderDate.split(" ")[0].split("/")[2]);
            int month = Integer.valueOf(sameDayReminderDate.split(" ")[0].split("/")[0]);
            int day = Integer.valueOf(sameDayReminderDate.split(" ")[0].split("/")[1]);
            int hour = Integer.valueOf(sameDayReminderDate.split(" ")[1].split(":")[0]);
            int min = Integer.valueOf(sameDayReminderDate.split(" ")[1].split(":")[1]);
            String fullDate = reserve.getFullScheduledDate();
            int paymentDay = Integer.valueOf(fullDate.split(" ")[0].split("/")[1]);

            Log.i(TAG, "setReminder: payment day -> " + paymentDay);
            Log.i(TAG, "setReminder: reminder day -> " + day);
            if (paymentDay == day)
                myIntent.putExtra("when", "Today");
            else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date date = format.parse(fullDate);
                    String dayOfTheWeek = (String) DateFormat.format("EEEE", date);
                    Log.i(TAG, "setReminder: day of week -> " + dayOfTheWeek);
                    myIntent.putExtra("when", dayOfTheWeek);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), reservePos, myIntent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);

            Log.d(TAG, "onCreate: calendar time -> " + calendar.getTime());
            Log.d(TAG, "onCreate: system time -> " + Calendar.getInstance().getTime());

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            pendingIntents.add(pendingIntent);
        }

        if (reserve.hasSecondaryReminder()) {
            String secondaryReminderDate = reserve.getSecondaryReminderDate();
            int year = Integer.valueOf(secondaryReminderDate.split(" ")[0].split("/")[2]);
            int month = Integer.valueOf(secondaryReminderDate.split(" ")[0].split("/")[0]);
            int day = Integer.valueOf(secondaryReminderDate.split(" ")[0].split("/")[1]);
            int hour = Integer.valueOf(secondaryReminderDate.split(" ")[1].split(":")[0]);
            int min = Integer.valueOf(secondaryReminderDate.split(" ")[1].split(":")[1]);
            String fullDate = reserve.getFullScheduledDate();
            int paymentDay = Integer.valueOf(fullDate.split(" ")[0].split("/")[1]);

            if (paymentDay == day)
                myIntent.putExtra("when", "Today");
            else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date date = format.parse(fullDate);
                    String dayOfTheWeek = (String) DateFormat.format("EEEE", date);
                    myIntent.putExtra("when", dayOfTheWeek);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext()
                    , reservePos * 100 + reservePos, myIntent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, min);

            calendar.set(Calendar.DAY_OF_MONTH, day - 1);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour);

            Log.d(TAG, "onCreate: calendar time -> " + calendar.getTime());
            Log.d(TAG, "onCreate: system time -> " + Calendar.getInstance().getTime());

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    interface SendReserves {
        void sendData(List<Reserve> reserves);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            SR = (SendReserves) getActivity();
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }
}
