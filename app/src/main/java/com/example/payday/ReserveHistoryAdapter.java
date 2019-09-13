package com.example.payday;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ReserveHistoryAdapter extends ArrayAdapter<Reserve> {
    private static final String TAG = "ReserveAdapter";
    private final Context context;
    private final List<Reserve> reserves;
    private String currentSymbol;
    private boolean elevated;

    public ReserveHistoryAdapter(Context context, List<Reserve> reserves, String symbol, boolean elevated) {
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
        convertView = inflater.inflate(R.layout.reserve_history_list_item, parent, false);

        TextView name = convertView.findViewById(R.id.tv_reserve_name);
        TextView separator = convertView.findViewById(R.id.tv_sep1);
        TextView goalAmount = convertView.findViewById(R.id.tv_reserve_goal_amount);
        TextView actualAmount = convertView.findViewById(R.id.tv_reserve_actual_amount);
        TextView date = convertView.findViewById(R.id.tv_reserve_subtraction_date);
        TextView priority = convertView.findViewById(R.id.tv_reserve_priority);

        assert reserve != null;
        name.setText(reserve.getName());
        goalAmount.setText(reserve.getGoalAmount() + "");
        actualAmount.setText(reserve.getActualAmount() + "");
        date.setText(reserve.getFullScheduledDate());
        if (reserve.getPriority() == 0) {
            priority.setText("Low");
            priority.setTextColor(ContextCompat.getColor(context, R.color.priorityLow));
            separator.setBackgroundColor(ContextCompat.getColor(context, R.color.priorityLow));
        }
        else
            if (reserve.getPriority() == 1) {
                priority.setText("Medium");
                priority.setTextColor(ContextCompat.getColor(context, R.color.priorityMedium));
                separator.setBackgroundColor(ContextCompat.getColor(context, R.color.priorityMedium));
            }
            else {
                priority.setText("High");
                priority.setTextColor(ContextCompat.getColor(context, R.color.priorityHigh));
                separator.setBackgroundColor(ContextCompat.getColor(context, R.color.priorityHigh));
            }

        return convertView;
    }
}
