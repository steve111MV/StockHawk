package com.udacity.stockhawk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import static com.udacity.stockhawk.sync.QuoteSyncJob.YEARS_OF_HISTORY;

/**
 * Created by STEVEN on 30/04/2017.
 */

public class DetailsAsyncTask extends AsyncTask<String,Object, Object[]> {
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Object[] doInBackground(String... params) {
        String stockSymbol = params[0];

        Calendar calendarToday = Calendar.getInstance();
        Calendar calendarLastYear = Calendar.getInstance();
        calendarLastYear.add(Calendar.YEAR, -YEARS_OF_HISTORY);
        Stock stock;
        List<HistoricalQuote> history;
        try {
            stock = YahooFinance.get(stockSymbol);
            history = stock.getHistory(calendarLastYear, calendarToday, Interval.WEEKLY);
            Log.i("stockS0", stockSymbol);
        } catch (IOException e) {
            e.printStackTrace();
            stock = null;
            history = null;
        }
        return new Object[]{stock, history};
    }
}
