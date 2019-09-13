package com.example.payday;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DashboardFragment extends Fragment {
    private Spinner currency;
    private TextView balance, reserveExpenses, monthIncome, reservedAmount;
    private FloatingActionButton addToBalance;
    private RelativeLayout balanceHolder, reserveHistoryHolder, monthIncomeHolder, reservesHolder
            , reservedAmountHolder, viewMoreRL;
    private ImageView swipe;
    private ViewPager pager;
    private MainPagerAdapter pagerAdapter;
    private ExpandableHeightListView reservesList;

    private User currentUser;
    private DBLinks dbLinks;
    private Intent parentIntent;
    private List<Operation> operations, thisMonthOperations, thisWeekOperations;
    private List<Reserve> finalisedReserves, unfinalisedReserves, upcomingReserves;
    private ReserveAdapter reserveAdapter;

    public static final String TAG = "Dashboard";

    public static DashboardFragment newInstance(int page, String title, Bundle bundleData, User user) {
        DashboardFragment dashboardFragment = new DashboardFragment();
        bundleData.putSerializable("currentUser", user);
        dashboardFragment.setArguments(bundleData);
        return dashboardFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        currentUser = (User) getArguments().getSerializable("currentUser");
        dbLinks = new DBLinks();
        operations = (List<Operation>) getArguments().getSerializable("operations");
        finalisedReserves = (List<Reserve>) getArguments().getSerializable("finalised_reserves");
        unfinalisedReserves = (List<Reserve>) getArguments().getSerializable("unfinalised_reserves");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        currency = view.findViewById(R.id.sp_currency);
        balance = view.findViewById(R.id.tv_balance);
        monthIncome = view.findViewById(R.id.tv_month_income);
        reserveExpenses = view.findViewById(R.id.tv_weekly_expenses);
        reservedAmount = view.findViewById(R.id.tv_reserved_amount);
        addToBalance = view.findViewById(R.id.fab_add_to_balance);
        balanceHolder = view.findViewById(R.id.rl1);
        reserveHistoryHolder = view.findViewById(R.id.rl2);
        monthIncomeHolder = view.findViewById(R.id.rl3);
        reservedAmountHolder = view.findViewById(R.id.rl4);
        reservesHolder = view.findViewById(R.id.rl5);
        viewMoreRL = view.findViewById(R.id.rl_view_more);
        swipe = view.findViewById(R.id.iv_swipe);
        reservesList = view.findViewById(R.id.ehl_upcoming_reserves);

        pager = Objects.requireNonNull(getActivity()).findViewById(R.id.viewpager);
        pagerAdapter = (MainPagerAdapter) pager.getAdapter();

        thisMonthOperations = new ArrayList<>();
        thisWeekOperations = new ArrayList<>();
        upcomingReserves = new ArrayList<>();

        viewMoreRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), GraphDetailsActivity.class);
                intent.putExtra("symbol", balance.getText().toString().split(" ")[0]);
                intent.putExtra("operations", (Serializable) operations);
                intent.putExtra("reserves", (Serializable) finalisedReserves);
                startActivity(intent);
            }
        });

        // TODO: check for any reserves from unfinalised reserves that have scheduled date after current date - done (in loginActivity)
        // if true -> remove those reserves from unfinalised reserves and add them to finalised reserves

        computeStuff(0, 0, 0, 0);
        Log.d(TAG, "onCreateView: finalised reserves size -> " + finalisedReserves.size());
        Log.d(TAG, "onCreateView: unfinalised reserves size -> " + unfinalisedReserves.size());
        computeFinalisedReserves(0, 0);
        computeReservedAmount(unfinalisedReserves);

        getCurrentWeekDateInterval();

        final List<String> currencyList = new ArrayList<>();
        currencyList.add("USD");
        currencyList.add("EUR");
        currencyList.add("GBP");
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.currency_spinner_item, currencyList);
        currency.setAdapter(currencyAdapter);

        final Map<String, String> symbols = new HashMap<>();
        symbols.put("USD", "$");
        symbols.put("EUR", "€");
        symbols.put("GBP", "£");

        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String symbol = symbols.get(currencyList.get(i));
                balance.setText(symbol + " " + balance.getText().toString().split(" ")[1]);
                monthIncome.setText(symbol + " " + monthIncome.getText().toString().split(" ")[1]);
                reserveExpenses.setText(symbol + " " + reserveExpenses.getText().toString().split(" ")[1]);
                // TODO: compute reserved amount and change text field - done
                reservedAmount.setText(symbol + " " + reservedAmount.getText().toString().split(" ")[1]);
                currentUser.setSymbol(symbol);
                pagerAdapter.setSymbol(symbol);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        reserveAdapter = new ReserveAdapter(getContext(), upcomingReserves, balance.getText().toString().split(" ")[0], false);
        reservesList.setDividerHeight(0);
        reservesList.setDivider(null);
        reservesList.setExpanded(true);
        reservesList.setAdapter(reserveAdapter);
        getUpcomingReserves(upcomingReserves);

        addToBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddToBalanceActivity.class);
                intent.putExtra("balance", currentUser.getBalance());
                intent.putExtra("currentUser", currentUser);
                startActivityForResult(intent, 1);
            }
        });

        balanceHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Click", Toast.LENGTH_SHORT).show();
            }
        });

        monthIncomeHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MonthIncomeHistoryActivity.class);
                intent.putExtra("current_symbol", balance.getText().toString().split(" ")[0]);
                intent.putExtra("month_operations", (Serializable) thisMonthOperations);
                startActivity(intent);
            }
        });

        reserveHistoryHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ReserveHistoryActivity.class);
                intent.putExtra("current_symbol", balance.getText().toString().split(" ")[0]);
                intent.putExtra("finalised_reserves", (Serializable) finalisedReserves);
                startActivity(intent);
            }
        });

        reservesHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(1);
            }
        });

        reservedAmountHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ReservedAmountActivity.class);
                intent.putExtra("reserves", (Serializable) unfinalisedReserves);
                intent.putExtra("symbol", balance.getText().toString().split(" ")[0]);
                startActivity(intent);
            }
        });

        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(1);
            }
        });



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                Operation operation = (Operation) data.getSerializableExtra("new_operation");
                int start = operations.size();
                operations.add(operation);
                int previousInc = Integer.valueOf(monthIncome.getText().toString().split(" ")[1]);
                int previousBalance = Integer.valueOf(balance.getText().toString().split(" ")[1]);
                int previousExpenses = Integer.valueOf(reserveExpenses.getText().toString().split(" ")[1]);
                computeStuff(start, previousInc, previousBalance, previousExpenses);

                //TODO: when reserve is finalised recalculate reserve expense history amount
                //int finalisedStart = finalisedReserves.size();
                //int previousBalance1 = Integer.valueOf(balance.getText().toString().split(" ")[1]);
                //computeFinalisedReserves(finalisedStart, previousBalance1);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    private String getFullCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @SuppressLint("SetTextI18n")
    private void computeStuff(int start, int previousInc, int previousBalance, int previousExpenses) {
        String currentMonth = getCurrentDate().split("/")[0];
        int income = previousInc;
        int balanceAmount = previousBalance;
        String weekInterval = getCurrentWeekDateInterval();
        String weekStart = weekInterval.split("-")[0];
        String weekEnd = weekInterval.split("-")[1];
        String symbol = balance.getText().toString().split(" ")[0];
        for (int i = start; i < operations.size(); i++) {
            String operationMonth = operations.get(i).getDate().split("/")[0];
            Operation operation = operations.get(i);
            if (operation.getType().equalsIgnoreCase("add"))
                balanceAmount += operation.getAmount();
            if (operationMonth.equals(currentMonth)
                    && operation.getType().equalsIgnoreCase("add")) {
                thisMonthOperations.add(operations.get(i));
                income += operation.getAmount();
            }
        }
        currentUser.setBalance(balanceAmount);
        monthIncome.setText(symbol + " " + income);
        balance.setText(symbol + " " + balanceAmount);
        //computeReservedAmount(reserves);
        //reserveExpenses.setText(symbol + " " + previousExpenses);
        pagerAdapter.setCurrentUser(currentUser);
        pagerAdapter.setSymbol(symbol);
        Log.d(TAG, "computeStuff: symbol -> " + symbol);
    }

    @SuppressLint("SetTextI18n")
    private void computeFinalisedReserves(int start, int previousExpenses) {
        int balanceAmount = Integer.valueOf(balance.getText().toString().split(" ")[1]);
        int finalisedReservesAmount = previousExpenses;
        String symbol = balance.getText().toString().split(" ")[0];
        for (int i = start; i < finalisedReserves.size(); i++) {
            balanceAmount -= finalisedReserves.get(i).getActualAmount();
            finalisedReservesAmount += finalisedReserves.get(i).getActualAmount();
        }
        reserveExpenses.setText(symbol + " " + finalisedReservesAmount);
        balance.setText(symbol + " " + balanceAmount);
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentWeekDateInterval() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DATE, -dayOfWeek + 1);

        Date weekStart = cal.getTime();
        cal.add(Calendar.DATE, dayOfWeek - 1);
        cal.add(Calendar.DATE, 7 - dayOfWeek);
        Date weekEnd = cal.getTime();

        return dateFormat.format(weekStart) + "-" + dateFormat.format(weekEnd);
    }

    @SuppressLint("SimpleDateFormat")
    private boolean dateAfter(String date1, String date2) {

        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date d1 = format.parse(date1);
            Date d2 = format.parse(date2);
            Log.d(TAG, "dateInBetween: d1 -> " + d1.toString());
            Log.d(TAG, "dateInBetween: d2 -> " + d2.toString());

            if (d1.after(d2))
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void computeReservedAmount(List<Reserve> reserves) {
        int reserved = Integer.valueOf(reservedAmount.getText().toString().split(" ")[1]);
        int balanceAmount = Integer.valueOf(balance.getText().toString().split(" ")[1]) + reserved;
        reserved = 0;
        String symbol = balance.getText().toString().split(" ")[0];
        for (int i = 0; i < reserves.size(); i++) {
            Reserve reserve = reserves.get(i);
            balanceAmount -= reserve.getActualAmount();
            reserved += reserve.getActualAmount();
            // TODO: if it is finalised it moves to expenses
        }

        balance.setText(symbol + " " + balanceAmount);
        reservedAmount.setText(symbol + " " + reserved);
        currentUser.setBalance(balanceAmount);
    }

    protected void receiveReserves(List<Reserve> reserves) {
        this.unfinalisedReserves = reserves;
        Log.d(TAG, "receiveReserves: size - > " + reserves.size());
        computeReservedAmount(this.unfinalisedReserves);
        upcomingReserves.clear();
        getUpcomingReserves(upcomingReserves);
        //reserveAdapter.notifyDataSetChanged();
    }

    protected void receiveFinalisedReserve(Reserve reserve) {

    }

    private void getUpcomingReserves(List<Reserve> upRes) {
        upRes.clear();
        List<Reserve> r = new ArrayList<>(unfinalisedReserves);
        List<Reserve> allReserves = sortByDate(r);
        if (allReserves.size() <= 3)
            upRes.addAll(allReserves);
        else {
            for (int i = 0; i < allReserves.size(); i++) {
                upRes.add(allReserves.get(i));
                if (i == 2)
                    break;
            }
        }
        reserveAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SimpleDateFormat")
    private List<Reserve> sortByDate(List<Reserve> reserves) {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        for (int i = 0; i < reserves.size() - 1; i++) {
            try {
                Date d1 = format.parse(reserves.get(i).getFullScheduledDate().split(" ")[0]);
                Date d2 = format.parse(reserves.get(i + 1).getFullScheduledDate().split(" ")[0]);

                if (d1.after(d2)) {
                    Reserve r = reserves.get(i);
                    reserves.set(i, reserves.get(i + 1));
                    reserves.set(i + 1, r);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return reserves;
    }
}
