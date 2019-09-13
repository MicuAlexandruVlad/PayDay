package com.example.payday;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ReservedAmountAdapter extends ArrayAdapter<Reserve> {
    private final Context context;
    private final List<Reserve> reserves;
    private String currentSymbol;

    public ReservedAmountAdapter(Context context, List<Reserve> reserves, String symbol) {
        super(context, -1, reserves);
        this.context = context;
        this.reserves = reserves;
        this.currentSymbol = symbol;
    }

    @NonNull
    @SuppressLint({"SetTextI18n", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Reserve reserve = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.reserved_amount_list_item, parent, false);

        TextView amount = convertView.findViewById(R.id.tv_reserve_actual_amount);
        TextView name = convertView.findViewById(R.id.tv_reserve_name);

        assert reserve != null;
        name.setText(reserve.getName());
        amount.setText(currentSymbol + " " + reserve.getActualAmount());

        return convertView;
    }
}
