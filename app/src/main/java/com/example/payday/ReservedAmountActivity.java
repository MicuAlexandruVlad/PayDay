package com.example.payday;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;

import java.util.List;
import java.util.Objects;

public class ReservedAmountActivity extends AppCompatActivity {
    private ExpandableHeightListView reservesList;
    private TextView total;

    private List<Reserve> reserves;
    private ReservedAmountAdapter adapter;
    private Intent parentIntent;
    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved_amount);

        Objects.requireNonNull(getSupportActionBar()).hide();

        reservesList = findViewById(R.id.ehl_reserves);
        total = findViewById(R.id.tv_total_reserved);

        parentIntent = getIntent();
        reserves = (List<Reserve>) parentIntent.getSerializableExtra("reserves");
        symbol = parentIntent.getStringExtra("symbol");

        adapter = new ReservedAmountAdapter(this, reserves, symbol);
        reservesList.setExpanded(true);
        reservesList.setAdapter(adapter);
        //reservesList.setDividerHeight(0);
        //reservesList.setDivider(null);

        computeTotal();
    }

    @SuppressLint("SetTextI18n")
    private void computeTotal() {
        int t = 0;
        for (int i = 0; i < reserves.size(); i++) {
            Reserve r = reserves.get(i);
            t += r.getActualAmount();
        }

        total.setText(symbol + " " + t);
    }
}
