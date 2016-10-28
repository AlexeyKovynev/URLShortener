package name.javalex.apijson.app.urlshortener.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import name.javalex.apijson.app.urlshortener.entities.LongShortDate;

public class DBExecutor {

    DBHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    private String shortUrl;
    private String longUrl;

    public boolean hasRows(Context context) {
        boolean flag;

        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        String queryString = "select exists(select 1 from " + DBHelper.TABLE_REQUEST_HISTORY + ");";
        database = dbHelper.getReadableDatabase();
        cursor = database.rawQuery(queryString, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        flag = count == 1;
        cursor.close();
        dbHelper.close();
        return flag;
    }

    public void addToDataBase(List<LongShortDate> longShortDateList, LongShortDate longShortDate, Context context) {

        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        database = dbHelper.getWritableDatabase();

        shortUrl = longShortDate.getShortLink();
        longUrl = longShortDate.getLongLink();

        //Write to DB
        writeToDB();

        //Update list with last position from DB
        cursor = database.query(DBHelper.TABLE_REQUEST_HISTORY, null, null, null, null, null, null);
        cursor.moveToLast();
        int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
        int shortUrlIndex = cursor.getColumnIndex(DBHelper.KEY_SHORT_URL);
        int longUrlIndex = cursor.getColumnIndex(DBHelper.KEY_ORIGINAL_URL);
        int dateTimeIndex = cursor.getColumnIndex(DBHelper.KEY_TIMESTAMP);
        LongShortDate newLongShortDate = new LongShortDate(cursor.getString(longUrlIndex), cursor.getString(shortUrlIndex),
                cursor.getString(dateTimeIndex), cursor.getInt(idIndex));
        cursor.close();
        dbHelper.close();
        longShortDateList.add(0, newLongShortDate);
    }

    public void addFirstRowToDataBase(List<LongShortDate> longShortDateList, LongShortDate longShortDate, Context context) {

        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        database = dbHelper.getWritableDatabase();

        shortUrl = longShortDate.getShortLink();
        longUrl = longShortDate.getLongLink();

        //Write to DB
        writeToDB();

        //Update list with last position from DB
        cursor = database.query(DBHelper.TABLE_REQUEST_HISTORY, null, null, null, null, null, null);
        cursor.moveToLast();
        int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
        int shortUrlIndex = cursor.getColumnIndex(DBHelper.KEY_SHORT_URL);
        int longUrlIndex = cursor.getColumnIndex(DBHelper.KEY_ORIGINAL_URL);
        int dateTimeIndex = cursor.getColumnIndex(DBHelper.KEY_TIMESTAMP);
        LongShortDate newLongShortDate = new LongShortDate(cursor.getString(longUrlIndex), cursor.getString(shortUrlIndex),
                cursor.getString(dateTimeIndex), cursor.getInt(idIndex));
        longShortDateList.add(0, newLongShortDate);

        cursor.close();
        dbHelper.close();
    }

    private void writeToDB() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_SHORT_URL, shortUrl);
        contentValues.put(DBHelper.KEY_ORIGINAL_URL, longUrl);
        contentValues.put(DBHelper.KEY_TIMESTAMP, DateAndTime.getCurrentDateAndTime());
        database.insert(DBHelper.TABLE_REQUEST_HISTORY, null, contentValues);
    }

    public void removeFromDB(List<LongShortDate> longShortDateList, List<Integer> rowsToBeRemoved) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (int i = 0; i < rowsToBeRemoved.size(); i++) {
            Integer currentId = new Integer(rowsToBeRemoved.get(i));
            longShortDateList.remove(currentId);
            database.delete(DBHelper.TABLE_REQUEST_HISTORY, DBHelper.KEY_ID + "=" + currentId, null);
        }
        dbHelper.close();
    }

    public void getDataFromDB(List<LongShortDate> longShortDateList) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        //Read from DB
        Cursor cursor = database.query(DBHelper.TABLE_REQUEST_HISTORY, null, null, null, null, null, null);
        if (cursor.moveToLast()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int shortUrlIndex = cursor.getColumnIndex(DBHelper.KEY_SHORT_URL);
            int longUrlIndex = cursor.getColumnIndex(DBHelper.KEY_ORIGINAL_URL);
            int dateTimeIndex = cursor.getColumnIndex(DBHelper.KEY_TIMESTAMP);
            do {
                LongShortDate longShortDate = new LongShortDate(cursor.getString(longUrlIndex), cursor.getString(shortUrlIndex),
                        cursor.getString(dateTimeIndex), cursor.getInt(idIndex));
                longShortDateList.add(longShortDate);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        dbHelper.close();
    }

    public LongShortDate getLastLongShortDateFromDB() {
        LongShortDate lastLongShortDate = null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        //Read from DB
        Cursor cursor = database.query(DBHelper.TABLE_REQUEST_HISTORY, null, null, null, null, null, null);
        if (cursor.moveToLast()) {
            int shortUrlIndex = cursor.getColumnIndex(DBHelper.KEY_SHORT_URL);
            int longUrlIndex = cursor.getColumnIndex(DBHelper.KEY_ORIGINAL_URL);
            lastLongShortDate = new LongShortDate(cursor.getString(longUrlIndex), cursor.getString(shortUrlIndex));
        }
        cursor.close();
        dbHelper.close();
        return lastLongShortDate;
    }
}
