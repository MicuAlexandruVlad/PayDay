package com.example.payday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;

import java.util.List;
import java.util.Objects;

public class MonthIncomeHistoryActivity extends AppCompatActivity {

    private ExpandableHeightListView monthList;

    private Intent parentIntent;
    private List<Operation> operations;
    private OperationAdapter adapter;
    private String currentSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_income_history);

        Objects.requireNonNull(getSupportActionBar()).hide();

        monthList = findViewById(R.id.ehl_month_income);

        parentIntent = getIntent();

        operations = (List<Operation>) parentIntent.getSerializableExtra("month_operations");
        currentSymbol = parentIntent.getStringExtra("current_symbol");

        adapter = new OperationAdapter(this, operations, currentSymbol);
        monthList.setExpanded(true);
        monthList.setAdapter(adapter);
        monthList.setDividerHeight(0);
        monthList.setDivider(null);
    }
}
