package name.javalex.apijson.app.urlshortener.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import name.javalex.apijson.app.urlshortener.entities.LongShortDate;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "URLShortenerDB";

    public static final String TABLE_REQUEST_HISTORY = "historyOfShortenings";
    public static final String KEY_ID = "_id";
    public static final String KEY_SHORT_URL = "shortUrl";
    public static final String KEY_ORIGINAL_URL = "originalUrl";
    public static final String KEY_TIMESTAMP = "timeStamp";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //Log.e("DB PATH", String.valueOf(context.getDatabasePath(DATABASE_NAME)));
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_REQUEST_HISTORY + "(" +
                KEY_ID + " integer primary key," +
                KEY_ORIGINAL_URL + " text," +
                KEY_SHORT_URL + " text," +
                KEY_TIMESTAMP + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
