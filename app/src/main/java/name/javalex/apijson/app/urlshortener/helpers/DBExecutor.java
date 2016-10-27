package name.javalex.apijson.app.urlshortener.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import name.javalex.apijson.app.urlshortener.entities.LongShortDate;

public class DBExecutor {

    DBHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    private String shortUrl;
    private String longUrl;

    public void addToDataBase(List<LongShortDate> longShortDateList, LongShortDate longShortDate, Context context) {

        if (dbHelper == null) {dbHelper = new DBHelper(context);}
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

        if (dbHelper == null) {dbHelper = new DBHelper(context);}
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
}
