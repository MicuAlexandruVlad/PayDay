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

public class OperationAdapter extends ArrayAdapter<Operation> {
    private final Context context;
    private final List<Operation> operations;
    private String currentSymbol;

    public OperationAdapter(Context context, List<Operation> operations, String symbol) {
        super(context, -1, operations);
        this.context = context;
        this.operations = operations;
        this.currentSymbol = symbol;
    }

    @NonNull
    @SuppressLint({"SetTextI18n", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Operation operation = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.operation_list_item, parent, false);

        TextView amount = convertView.findViewById(R.id.tv_amount);
        TextView date = convertView.findViewById(R.id.tv_date);
        TextView from = convertView.findViewById(R.id.tv_from);
        ImageView type = convertView.findViewById(R.id.iv_operation_type);

        assert operation != null;
        if (operation.getType().equalsIgnoreCase("add")) {
            type.setImageResource(R.drawable.money_in);
            amount.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.moneyIn)));
        }
        if (operation.getType().equalsIgnoreCase("change")) {
            type.setImageResource(R.drawable.budget_change);
            amount.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.moneyChange)));
        }
        if (operation.getType().equalsIgnoreCase("expense")) {
            type.setImageResource(R.drawable.money_out);
            amount.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.moneyOut)));
        }

        date.setText(operation.getDate());
        from.setText(operation.getFrom());
        amount.setText(currentSymbol + " " + operation.getAmount());

        return convertView;
    }
}
