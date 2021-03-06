package com.android.example.comesanews.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * This class serves as the ContentProvider for all of News's data. This class allows us to
 * insert data, query data, and delete data.
 */
public class NewsProvider extends ContentProvider {

    // These constant will be used to match URIs with the data they are looking for.
    public static final int CODE_NEWS = 100;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private NewsDBHelper mOpenHelper;

    // Creates the UriMatcher that will match each URI to the CODE_NEWS constant defined above.
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, NewsContract.PATH_NEWS, CODE_NEWS);
        return matcher;
    }

    //In onCreate, we initialize our content provider on startup.
    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        /*
         * Given a URI, will determine what kind of request is being made and query the database
         * accordingly.
         */
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.LatestNewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * We aren't going to do anything with this method. However, we are required to override it as
     * WeatherProvider extends ContentProvider.
     */
    @Override
    public String getType(Uri uri) {
        throw new RuntimeException("We are not implementing getType.");
    }

    /**
     * We aren't going to do anything with this method. However, we are required to
     * override it as NewsProvider extends ContentProvider.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        return null;
    }

    // Handles requests to insert a set of new rows.
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(NewsContract.LatestNewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

            // If the URI does match match CODE_NEWS, return the super implementation of bulkInsert
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // Deletes data at a given URI with optional arguments for more fine tuned deletions.
    @Override
    public int delete(Uri uri, String selection,
                      String[] selectionArgs) {
        int numRowsDeleted;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_NEWS:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        NewsContract.LatestNewsEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("We will implement the update method!");
    }

}
