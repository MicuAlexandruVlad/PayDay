package com.example.payday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;

import java.util.List;
import java.util.Objects;

public class ReserveHistoryActivity extends AppCompatActivity {
    private ExpandableHeightListView weekList;

    private Intent parentIntent;
    private List<Reserve> finalisedReserves;
    private ReserveHistoryAdapter adapter;
    private String currentSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_history);

        Objects.requireNonNull(getSupportActionBar()).hide();

        weekList = findViewById(R.id.ehl_week_expenses);

        parentIntent = getIntent();
        finalisedReserves = (List<Reserve>) parentIntent.getSerializableExtra("finalised_reserves");
        currentSymbol = parentIntent.getStringExtra("current_symbol");

        //TODO: make a different adapter for this one
        adapter = new ReserveHistoryAdapter(this, finalisedReserves, currentSymbol, true);
        weekList.setAdapter(adapter);
        weekList.setExpanded(true);
        weekList.setDividerHeight(0);
        weekList.setDivider(null);
    }
}
