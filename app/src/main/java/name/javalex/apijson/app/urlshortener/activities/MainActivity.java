package name.javalex.apijson.app.urlshortener.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import name.javalex.apijson.app.urlshortener.R;
import name.javalex.apijson.app.urlshortener.adapters.ListAdapter;
import name.javalex.apijson.app.urlshortener.entities.LongShortDate;
import name.javalex.apijson.app.urlshortener.helpers.DBHelper;
import name.javalex.apijson.app.urlshortener.entities.RequestData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView resultTextView, qrWaitingTextView;
    private EditText targetEditText;
    private ImageButton getQRbtn;

    private final static String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyDUs8hh8hN9gBm9Cqwg2EUSJ-GCcezGcKE";
    private final static String REQUEST_QR_CODE_URL = "http://chart.googleapis.com/chart?cht=qr&chs=547x547&choe=UTF-8&chld=H&chl=";

    private String shortUrl = "";
    private String pasteText = "";
    private String targetText = "";
    //private String shortenedURL = "";
    private String genQRcodeURL = "";
    private String[] test = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10", "Item 11"};

    DownloadImage downloadImage;
    Dialog dialog;
    Bitmap bitmap;
    ImageView img;
    Gson gson;
    ClipData clip;
    ClipboardManager clipboard;
    RequestData requestData;
    DBHelper dbHelper;
    ListView listView;
    ListAdapter listAdapter;

    public String getResultTextView() {
        return resultTextView.getText().toString();
    }

    public void setResultTextView(String result) {
        this.resultTextView.setText(result);
    }

    public String getTargetEditText() {
        return targetText = targetEditText.getText().toString();
    }

    public void setTargetEditText(String text) {
        this.targetEditText.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide keyboard on start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getQRbtn = (ImageButton) findViewById(R.id.btnGetQR);
        getQRbtn.setOnClickListener(this);

        Button shortenBtn = (Button) findViewById(R.id.btnSubmit);
        shortenBtn.setOnClickListener(this);

        Button copyBtn = (Button) findViewById(R.id.btnCopy);
        copyBtn.setOnClickListener(this);

        Button pasteBtn = (Button) findViewById(R.id.btnPaste);
        pasteBtn.setOnClickListener(this);

        targetEditText = (EditText) findViewById(R.id.editTextTargetUrl);
        resultTextView = (TextView) findViewById(R.id.textViewResult);
        qrWaitingTextView = (TextView) findViewById(R.id.textViewQRWaiting);

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        dbHelper = new DBHelper(this);

        listView = (ListView) findViewById(R.id.listView);

        //Retrieve data from database in background and fill the list view
        FillListView fillListView = new FillListView();
        fillListView.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPaste: {
                if (clipboard.hasPrimaryClip()) {
                    clip = clipboard.getPrimaryClip();

                    if (clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        ClipData.Item item = clip.getItemAt(0);
                        pasteText = item.getText().toString();
                    }
                } else {
                    Toast.makeText(this, "Clipboard is empty.\nCopy link first please", Toast.LENGTH_LONG).show();
                }
                if (!TextUtils.isEmpty(pasteText)) {
                    setTargetEditText(pasteText);
                }
            }
            break;
            case R.id.btnCopy: {
                if (shortUrl.startsWith("http")) {
                    clip = ClipData.newPlainText("text", shortUrl);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No results yet", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.btnSubmit: {
                //Verify field not empty
                if (!(getTargetEditText().matches(""))) {
                    //Verify internet connected
                    if (isOnline()) {
                        requestData = new RequestData();
                        requestData.setLongUrl(targetText);
                        setResultTextView("Working...");
                        try {
                            //Serialize to JSON and send request
                            sendRequest(serialize(requestData));
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (UnknownHostException err) {
                            Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                            setResultTextView("No internet connection");
                            err.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please provide URL", Toast.LENGTH_SHORT).show();
                    setResultTextView("");
                }
            }
            break;
            case R.id.btnGetQR: {
                getQRbtn.setVisibility(View.GONE);
                shortUrl = getResultTextView();
                //Check internet connection
                if (isOnline()) {
                    //Check URL present
                    if (shortUrl.startsWith("http")) {
                        //Concatenate template with shortened URL
                        genQRcodeURL = REQUEST_QR_CODE_URL + shortUrl;
                        downloadImage = new DownloadImage();
                        //Download QR code in background and display
                        downloadImage.execute();
                    } else {
                        Toast.makeText(this, "Cannot generate QR code", Toast.LENGTH_LONG).show();
                        getQRbtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                    getQRbtn.setVisibility(View.VISIBLE);
                }
            }
            break;
        }
    }

    //Convert request body to JSON
    public String serialize(RequestData requestData) {
        gson = new Gson();
        return gson.toJson(requestData);
    }

    public void sendRequest(String body) throws JSONException, UnsupportedEncodingException, UnknownHostException {
        ByteArrayEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), REQUEST_URL, entity, "application/json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                //Log.e("RESPONSE ", obj.toString());
                try {
                    shortUrl = obj.getString("id");
                    setResultTextView(shortUrl);
                    addToDataBase();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured :(", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    setResultTextView("");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("RESPONSE ", errorResponse.toString());

                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Internal Server Error,\ntry Later please", Toast.LENGTH_LONG).show();
                } else if (statusCode == 403) {
                    Toast.makeText(getApplicationContext(), "Wrong data provided", Toast.LENGTH_LONG).show();
                } else if (statusCode == 400) {
                    Toast.makeText(getApplicationContext(), "This link cannot be shortened", Toast.LENGTH_LONG).show();
                } else if (statusCode == 0) {
                    Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Terrible error occurred: " + statusCode + "\n" + throwable.toString(), Toast.LENGTH_LONG).show();
                }
                //Clear result text view
                setResultTextView("");
            }
        });
    }

    private void addToDataBase() {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.e("DATE and TIME", currentDateTimeString);

        //Write to DB
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_SHORT_URL, shortUrl);
        contentValues.put(DBHelper.KEY_ORIGINAL_URL, getTargetEditText());
        contentValues.put(DBHelper.KEY_TIMESTAMP, currentDateTimeString);
        database.insert(DBHelper.TABLE_REQUEST_HISTORY, null, contentValues);

        //Read from DB
        Cursor cursor = database.query(DBHelper.TABLE_REQUEST_HISTORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int shortUrlIndex = cursor.getColumnIndex(DBHelper.KEY_SHORT_URL);
            int longUrlIndex = cursor.getColumnIndex(DBHelper.KEY_ORIGINAL_URL);
            int dateTimeIndex = cursor.getColumnIndex(DBHelper.KEY_TIMESTAMP);
            do {
                Log.e("DB DATA", "ID = " + cursor.getInt(idIndex) +
                        ", SU = " + cursor.getString(shortUrlIndex) +
                        ", LU = " + cursor.getString(longUrlIndex) +
                        ", TS = " + cursor.getString(dateTimeIndex));
            } while (cursor.moveToNext());
        } else {
            Log.e("DB DATA", "0 rows");
        }
        cursor.close();
        dbHelper.close();
    }

    class DownloadImage extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            qrWaitingTextView.setVisibility(View.VISIBLE);
            qrWaitingTextView.setText("Generating QR code...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(genQRcodeURL);
                URLConnection conn = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpConn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            qrWaitingTextView.setText("");
            qrWaitingTextView.setVisibility(View.GONE);

            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.qr);
            dialog.setCancelable(true);
            dialog.setTitle("Your QR code");
            img = (ImageView) dialog.findViewById(R.id.imageViewQR);
            //img.setScaleType(ImageView.ScaleType.FIT_XY);
            dialog.show();
            img.setImageBitmap(bitmap);
            getQRbtn.setVisibility(View.VISIBLE);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Dismiss dialog if it exists when user rotates screen or switches to another app
        if (!(dialog == null)) {
            dialog.dismiss();
        }
    }

    public boolean hasRows() {
        boolean flag;
        String queryString = "select exists(select 1 from " + DBHelper.TABLE_REQUEST_HISTORY  + ");";
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(queryString, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (count == 1) {
            flag = true;
        } else {
            flag = false;
        }
        cursor.close();
        dbHelper.close();
        return flag;
    }


    class FillListView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            qrWaitingTextView.setVisibility(View.VISIBLE);
            qrWaitingTextView.setText("Retrieving your history...");
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.loading_overlay);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SQLiteDatabase database = dbHelper.getReadableDatabase();
            List<LongShortDate> longShortDateList = new ArrayList<>();


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
            } else {
                Log.e("DB DATA", "0 rows");
            }
            cursor.close();
            dbHelper.close();
            listAdapter = new ListAdapter(MainActivity.this, R.layout.list_layout, longShortDateList);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            listView.setAdapter(listAdapter);

            qrWaitingTextView.setText("");
            qrWaitingTextView.setVisibility(View.GONE);
            dialog.dismiss();
        }
    }

}
