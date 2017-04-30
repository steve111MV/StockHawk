package com.udacity.stockhawk.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Tools;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    public static final String EXTRA_STOCK_SYMBOL = "com.udacity.stockhawk.symbol";

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.fab)
    FloatingActionButton fab;

    /*@SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;*/
    private StockAdapter adapter;

    //Scrlling behavior
    int fabReturnY = 0;
    int phoneDensity = 160;
    int fabQuickReturnSize = 100;
    boolean isFabVisible = true;

    @Override
    public void onClick(String symbol) {

        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_STOCK_SYMBOL, symbol);
        startActivity(intent);
        //Timber.d("Symbol clicked: %s", symbol);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        //init Floating hiding dimension when RecyclerView scrolls
        fabQuickReturnSize = Tools.dpToPx(this, 80);
        //phoneDensity = (int) getResources().getDisplayMetrics().density;

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);

        stockRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Callback method to be invoked when the RecyclerView has been scrolled. This will be
             * called after the scroll has completed.
             * <p>
             * This callback will also be called if visible item range changes after a layout
             * calculation. In that case, dx and dy will be 0.
             *
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx           The amount of horizontal scroll.
             * @param dy           The amount of vertical scroll.
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.i("fabState"," in onSCrolled, preparing check: "+(40 * phoneDensity)+" -- y="+fabReturnY);

                if(isFabVisible && fabReturnY > 60 *10){
                    hideFabMenu(300);
                    Log.i("fabState","hidden");
                    isFabVisible = false;
                    fabReturnY = 0;
                } else if(!isFabVisible && fabReturnY < -60  * 10){
                    showFabMenu(300);
                    Log.i("fabState","visible");
                    isFabVisible =true;
                    fabReturnY = 0;
                }

                if (Math.signum(dy) != Math.signum(fabReturnY))
                    fabReturnY = 0;
                fabReturnY += dy;
            }
        });
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            //error.setText(getString(R.string.error_no_network));
            //error.setVisibility(View.VISIBLE);

            Snackbar.make(fab,
                    getString(R.string.error_no_network),
                    Snackbar.LENGTH_LONG).show();

        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(fab,
                    getString(R.string.toast_no_connectivity),
                    Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);

            Snackbar.make(getWindow().getDecorView(),
                    getString(R.string.error_no_stocks),
                    Snackbar.LENGTH_LONG).show();
            //error.setText(getString(R.string.error_no_stocks));
            //error.setVisibility(View.VISIBLE);
        } else {
            //error.setVisibility(View.GONE);
        }
    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            //error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Shows the FloatingActionButon with a reverse translate animation
    void showFabMenu(int durationMillis) {
            ViewCompat
                    .animate(fab)
                    .translationY(0).setDuration(durationMillis);
    }

    // Hides the FloatingActionButon with a translate animation
    void hideFabMenu(int durationMillis) {
        ViewCompat
                .animate(fab)
                //.translationY(fabQuickReturnSize)
                .translationY(fabQuickReturnSize)
                .setDuration(durationMillis);
    }
}
