package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

public class WidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{
                                QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                remoteViews.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex
                        ("symbol")));

                remoteViews.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex(
                        "bid_price")));

                if (data.getInt(data.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                    remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                if (Utils.showPercent) {
                    remoteViews.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                } else {
                    remoteViews.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
                }
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data != null && data.moveToPosition(position)) {
                    final int QUOTES_ID_COL = 0;
                    return data.getLong(QUOTES_ID_COL);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}