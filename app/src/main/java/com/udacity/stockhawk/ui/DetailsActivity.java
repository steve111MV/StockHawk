package com.udacity.stockhawk.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
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

import static android.R.attr.value;

public class DetailsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener {


    private String mStockSymbol;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.lowChartView)
    LineChart mLowEntriesLineChart;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.highChartView)
    LineChart mHighEntriesLineChart;

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

                    setTitle(stock.getName());
                    /*
                    Log.i("stockSize", history.size()+"");
                    Log.i("stockSCurrency", stock.getCurrency()+"");
                    Log.i("stockSNAME", stock.getName());
                    Log.i("stockSHistorySize", history.size()+" cal");*/

                    try {
                        LineDataSet highLineSet, lowLineSet;

                        List<String> dateLabels = new ArrayList<String>();
                        List<Entry> lowEntries = new ArrayList<Entry>();
                        List<Entry> highEntries = new ArrayList<Entry>();

                        String tmpDate = null;
                        int tmpValue;
                        int lowEntryMax = 0;
                        int highEntryMax = 0;
                        int index = 0;

                        for (HistoricalQuote stockHistory : history) {
                            tmpValue = stockHistory.getLow().intValue();
                            if (tmpValue > lowEntryMax)
                                lowEntryMax = tmpValue;

                            tmpValue = stockHistory.getHigh().intValue();
                            if (tmpValue > highEntryMax)
                                highEntryMax = tmpValue;

                            try {
                                //tmpDate = stockHistory.getDate();
                                tmpDate = "May 10th 2017";
                                lowEntries.add(new Entry(stockHistory.getLow().floatValue(), index));
                                highEntries.add(new Entry(stockHistory.getHigh().floatValue(), index++));
                                dateLabels.add(tmpDate);
                            } catch (ParseException e) {
                            }
                        }

                        lowLineSet = new LineDataSet(lowEntries, "Stock Bid Price");
                        highLineSet = new LineDataSet(highEntries, "Stock Bid Price");
                        lowLineSet.setDrawFilled(true);
                        highLineSet.setDrawFilled(true);

                        //lowLineSet.setColors(Color);
                        //highLineSet.setColors(ColorTemplate.COLORFUL_COLORS);

                        /*
                        LineData lowLineData = new LineData(dateLabels, lowLineSet);
                        LineData highLineData = new LineData(dateLabels, highLineSet);
                        */
                        LineData lowLineData = new LineData(lowLineSet);
                        LineData highLineData = new LineData(highLineSet);
                        //graphLineChart.setDescription(getString(R.string.stock_low_tag_line));
                        //mHighLineChart.setDescription(getString(R.string.stock_high_tag_line));

                        //mLowEntriesLineChart.setData(lowLineData);
                        //mLowEntriesLineChart.notifyDataSetChanged();
                        //mLowEntriesLineChart.invalidate();
                        mHighEntriesLineChart.setData(highLineData);
                        mHighEntriesLineChart.notifyDataSetChanged();
                        mHighEntriesLineChart.invalidate();

                        /*
                        List<Entry> entries = new ArrayList<>();
                        int start = 0;
                        for (HistoricalQuote iq : history) {
                            entries.add(new Entry(start, iq.getClose().floatValue()));
                            start++;
                        }

                        LineDataSet lineDataSet = new LineDataSet(entries, stock.getCurrency());
                        LineData lineData = new LineData(lineDataSet);
                        graphLineChart.setData(lineData);
                        graphLineChart.invalidate();
                        */

                    } catch (IndexOutOfBoundsException|NullPointerException e) {
                        e.printStackTrace();
                        setTitle(stock.getSymbol());
                    }

                    swipeRefreshLayout.setRefreshing(false);
            }}.execute(mStockSymbol);
        }
    }

}