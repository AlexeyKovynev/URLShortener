package name.javalex.apijson.app.urlshortener.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
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
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import name.javalex.apijson.app.urlshortener.R;
import name.javalex.apijson.app.urlshortener.adapters.ListAdapter;
import name.javalex.apijson.app.urlshortener.entities.LongShortDate;
import name.javalex.apijson.app.urlshortener.helpers.DBExecutor;
import name.javalex.apijson.app.urlshortener.entities.RequestData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public List<LongShortDate> longShortDateList;
    public List<Integer> rowsToBeRemoved;

    ImageButton getQRbtn, deleteBtn;
    DownloadImage downloadImage;
    Dialog dialog;
    AlertDialog confirmDelete;
    Bitmap bitmap;
    ImageView img;
    Gson gson;
    ClipData clip;
    ClipboardManager clipboard;
    RequestData requestData;
    ListView listView;
    ListAdapter listAdapter;
    LongShortDate lastLongShortDate, newLongShortDate;
    DBExecutor dbExecutor;

    private TextView resultTextView, qrWaitingTextView;
    private EditText targetEditText;
    private final static String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?key=4w5y4e5yw45yw45yw45yw45y";
    private final static String REQUEST_QR_CODE_URL = "http://chart.googleapis.com/chart?cht=qr&chs=547x547&choe=UTF-8&chld=H&chl=";

    private String shortUrl = "";
    private String pasteText = "";
    private String targetText = "";
    private String genQRcodeURL = "";

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

        longShortDateList = new ArrayList<>();

        getQRbtn = (ImageButton) findViewById(R.id.btnGetQR);
        getQRbtn.setOnClickListener(this);
        getQRbtn.setVisibility(View.GONE);

        deleteBtn = (ImageButton) findViewById(R.id.btnDelete);
        deleteBtn.setOnClickListener(this);
        deleteBtn.setVisibility(View.GONE);


        Button shortenBtn = (Button) findViewById(R.id.btnSubmit);
        shortenBtn.setOnClickListener(this);

        Button copyBtn = (Button) findViewById(R.id.btnCopy);
        copyBtn.setOnClickListener(this);

        Button pasteBtn = (Button) findViewById(R.id.btnPaste);
        pasteBtn.setOnClickListener(this);

        targetEditText = (EditText) findViewById(R.id.editTextTargetUrl);
        resultTextView = (TextView) findViewById(R.id.textViewResult);
        qrWaitingTextView = (TextView) findViewById(R.id.textViewQRWaiting);
        qrWaitingTextView.setVisibility(View.GONE);

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        dbExecutor = new DBExecutor();

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //Retrieve data from database in background and fill the list view
        if (dbExecutor.hasRows(this)) {
            new FillListView().execute();
        }
        rowsToBeRemoved = new LinkedList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listView.isItemChecked(i)) {
                    rowsToBeRemoved.add(longShortDateList.get(i).getId());
                } else {
                    int index = rowsToBeRemoved.indexOf(longShortDateList.get(i).getId());
                    rowsToBeRemoved.remove(index);
                }

                if (listView.getCheckedItemCount() == 1) {
                    setResultTextView(longShortDateList.get(i).getShortLink());
                    deleteBtn.setVisibility(View.VISIBLE);
                    getQRbtn.setVisibility(View.VISIBLE);
                } else if (listView.getCheckedItemCount() > 1) {
                    deleteBtn.setVisibility(View.VISIBLE);
                    getQRbtn.setVisibility(View.GONE);
                    setResultTextView("");
                } else {
                    deleteBtn.setVisibility(View.GONE);
                    getQRbtn.setVisibility(View.GONE);
                    setResultTextView("");
                }

                longShortDateList.get(i).getId();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPaste: {
                if (clipboard.hasPrimaryClip()) {
                    clip = clipboard.getPrimaryClip();
                    //Get data from buffer if it is plain text
                    if (clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        ClipData.Item item = clip.getItemAt(0);
                        pasteText = item.getText().toString();
                        targetEditText.setSelection(targetEditText.getText().length());
                    }
                } else {
                    Toast.makeText(this, R.string.empty_clipboard, Toast.LENGTH_LONG).show();
                }
                if (!TextUtils.isEmpty(pasteText)) {
                    setTargetEditText(pasteText);
                    targetEditText.setSelection(targetEditText.getText().length());
                }
            }
            break;
            case R.id.btnCopy: {
                if (shortUrl.startsWith("http")) {
                    clip = ClipData.newPlainText("text", getResultTextView());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
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
                        setResultTextView(getString(R.string.working));
                        getQRbtn.setVisibility(View.GONE);
                        try {
                            //Serialize to JSON and send request
                            sendRequest(serialize(requestData));
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (UnknownHostException err) {
                            Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
                            setResultTextView(getString(R.string.no_internet_message));
                            getQRbtn.setVisibility(View.GONE);
                            err.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.no_link_message, Toast.LENGTH_SHORT).show();
                    setResultTextView("");
                    getQRbtn.setVisibility(View.GONE);
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
                        //Download QR code in display
                        downloadImage.execute();
                    } else {
                        Toast.makeText(this, R.string.cannot_generate_qr_message, Toast.LENGTH_LONG).show();
                        getQRbtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_SHORT).show();
                    getQRbtn.setVisibility(View.VISIBLE);
                }
            }
            break;
            case R.id.btnDelete: {
                deleteBtn.setVisibility(View.GONE);
                getQRbtn.setVisibility(View.GONE);
                removeFromDBandFromList();
                listView.clearChoices();
                setResultTextView("");
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
                try {
                    shortUrl = obj.getString("id");
                    setResultTextView(shortUrl);
                    getQRbtn.setVisibility(View.VISIBLE);
                    newLongShortDate = new LongShortDate(targetText, shortUrl);
                    //Check is this row exist in history
                    AddToDBAndUpdateList addToDBAndUpdateList = new AddToDBAndUpdateList();
                    addToDBAndUpdateList.execute();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.error_occurred_message, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    setResultTextView("");
                    getQRbtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!(errorResponse == null)) {
                    Log.e("UNDEFINED RESPONSE", errorResponse.toString());
                }
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), R.string.error_404_message, Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), R.string.error_500_message, Toast.LENGTH_LONG).show();
                } else if (statusCode == 403) {
                    Toast.makeText(getApplicationContext(), R.string.error_403_message, Toast.LENGTH_LONG).show();
                } else if (statusCode == 400) {
                    Toast.makeText(getApplicationContext(), R.string.error_400_message, Toast.LENGTH_LONG).show();
                } else if (statusCode == 0) {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.undefined_error_message) + statusCode + "\n" + throwable.toString(), Toast.LENGTH_LONG).show();
                }
                //Clear result text view
                setResultTextView("");
                getQRbtn.setVisibility(View.GONE);
            }
        });
    }

    class DownloadImage extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            qrWaitingTextView.setVisibility(View.VISIBLE);
            qrWaitingTextView.setText(R.string.generating_code_message);
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
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.qr);
            dialog.setCancelable(true);


            //dialog.setTitle("Your QR code");
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
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Dismiss dialog if it exists when user rotates screen or switches to another app
        if (!(dialog == null)) {
            dialog.dismiss();
        }
        if (!(confirmDelete == null)) {
            confirmDelete.dismiss();
        }
    }

    //Fill out the list view on program start
    class FillListView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(MainActivity.this, R.style.ProgressDialog);
            dialog.setContentView(R.layout.loading_overlay);
            dialog.setCancelable(false);
            dialog.show();
            longShortDateList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dbExecutor.getDataFromDB(longShortDateList);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (listAdapter == null) {
                listAdapter = new ListAdapter(MainActivity.this, R.layout.list_layout, longShortDateList);
                listView.setAdapter(listAdapter);
            } else {
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    //Check is this line already exist in database in last position
    class AddToDBAndUpdateList extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(MainActivity.this, R.style.ProgressDialog);
            dialog.setContentView(R.layout.loading_overlay);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            lastLongShortDate = dbExecutor.getLastLongShortDateFromDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (lastLongShortDate == null) {
                dbExecutor.addFirstRowToDataBase(longShortDateList, newLongShortDate, getApplicationContext());
            } else if (!(lastLongShortDate.equals(newLongShortDate))) {
                if (listView.getCheckedItemCount() > 0) {
                    rowsToBeRemoved.clear();
                    listView.clearChoices();
                    deleteBtn.setVisibility(View.GONE);
                }
                dbExecutor.addToDataBase(longShortDateList, newLongShortDate, getApplicationContext());

            }
            listAdapter = new ListAdapter(MainActivity.this, R.layout.list_layout, longShortDateList);
            listView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }
    }

    protected void removeFromDBandFromList() {

        confirmDelete = new AlertDialog.Builder(this)
                .setMessage(R.string.delete_confirmation).setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dbExecutor.removeFromDB(longShortDateList, rowsToBeRemoved);
                        new FillListView().execute();
                        Toast.makeText(MainActivity.this, R.string.removed_from_history_message, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        listAdapter.notifyDataSetChanged();
                        rowsToBeRemoved.clear();
                    }
                }).show();
    }
}