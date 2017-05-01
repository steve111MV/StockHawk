package com.udacity.stockhawk.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.DetailsAsyncTask;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class DetailsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener {


    private String mStockSymbol;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chartView)
    LineChart graphLineChart;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        if(getIntent()!=null) {
            mStockSymbol = getIntent().getStringExtra(MainActivity.EXTRA_STOCK_SYMBOL);
        }

        swipeRefreshLayout.setOnRefreshListener(this);
        //swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            //error.setText(getString(R.string.error_no_network));
            //error.setVisibility(View.VISIBLE);

            Snackbar.make(getWindow().getDecorView(),
                    getString(R.string.error_no_network),
                    Snackbar.LENGTH_LONG).show();

        } else {
            swipeRefreshLayout.setRefreshing(true);

            new DetailsAsyncTask(){
                @Override
                protected void onPostExecute(Object[] objects) {
                    //super.onPostExecute(stock);

                    Stock stock = (Stock) objects[0];
                    List<HistoricalQuote> history = (List<HistoricalQuote>) objects[1];


                        /*
                        setTitle(stock.getName());
                        Log.i("stockSize", stock.getHistory().size()+"");
                        Log.i("stockSCurrency", stock.getCurrency()+"");
                        Log.i("stockSNAME", stock.getName());
                        Log.i("stockSHistorySize", history.size()+" cal");
                        */

                    try {
                        history = stock.getHistory();

                        List<Entry> entries = new ArrayList<>();
                        for (HistoricalQuote iq : history) {
                            entries.add(new Entry(0f, iq.getHigh().floatValue()));
                        }

                        LineDataSet lineDataSet = new LineDataSet(entries, stock.getCurrency());
                        LineData lineData = new LineData(lineDataSet);
                        graphLineChart.setData(lineData);
                        graphLineChart.invalidate();

                    } catch (IOException|NullPointerException e) {
                        e.printStackTrace();
                        setTitle(stock.getSymbol());
                    }

                    swipeRefreshLayout.setRefreshing(false);
            }}.execute(mStockSymbol);
        }
    }

}