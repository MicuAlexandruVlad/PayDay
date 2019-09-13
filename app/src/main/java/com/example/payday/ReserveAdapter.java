package com.example.payday;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ReserveAdapter extends ArrayAdapter<Reserve> {
    private static final String TAG = "ReserveAdapter";
    private final Context context;
    private final List<Reserve> reserves;
    private String currentSymbol;
    private boolean elevated;

    public ReserveAdapter(Context context, List<Reserve> reserves, String symbol, boolean elevated) {
        super(context, -1, reserves);
        this.context = context;
        this.reserves = reserves;
        this.currentSymbol = symbol;
        this.elevated = elevated;
    }

    @NonNull
    @SuppressLint({"SetTextI18n", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Reserve reserve = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.reserve_list_item, parent, false);

        TextView reserveName = convertView.findViewById(R.id.tv_reserve_name);
        TextView goalAmount = convertView.findViewById(R.id.tv_reserve_goal_amount);
        TextView firstLetter = convertView.findViewById(R.id.tv_reserve_name_first_letter);
        CardView priority = convertView.findViewById(R.id.cv_reserve_priority_color);
        ImageView reminder = convertView.findViewById(R.id.iv_reminder);
        ProgressBar progress = convertView.findViewById(R.id.pb_reserve_goal_amount_progress);
        RelativeLayout elevatedBody = convertView.findViewById(R.id.rl_elevated_body);

        assert reserve != null;

        if (reserve.getPriority() == 0)
            priority.setCardBackgroundColor(ContextCompat.getColor(context, R.color.priorityLow));
        else if (reserve.getPriority() == 1)
            priority.setCardBackgroundColor(ContextCompat.getColor(context, R.color.priorityMedium));
        else
            priority.setCardBackgroundColor(ContextCompat.getColor(context, R.color.priorityHigh));

        if (!elevated)
            elevatedBody.setElevation(0f);
        reserveName.setText(reserve.getName());
        firstLetter.setText(reserve.getName().toCharArray()[0] + "");
        goalAmount.setText(currentSymbol + " " + reserve.getGoalAmount());
        progress.setMax(reserve.getGoalAmount());
        progress.setProgress(reserve.getActualAmount());
        if (reserve.hasReminderForScheduledDate() || reserve.hasSecondaryReminder())
            reminder.setImageResource(R.drawable.reminder);
        else
            reminder.setImageResource(R.drawable.reminder_off);


        return convertView;
    }
}
