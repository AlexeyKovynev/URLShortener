package name.javalex.apijson.app.urlshortener.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import name.javalex.apijson.app.urlshortener.R;
import name.javalex.apijson.app.urlshortener.urlEntity.RequestData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public String id = "";
    private String pasteText = "http://stackoverflow.com/search?q=URL+Shortener+api+java";
    private String targetText = "";
    private TextView resultTextView, qrWaitingTextView;
    private EditText targetEditText;
    ImageButton getQRbtn;

    private final static String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyDUs8hh8hN9gBm9Cqwg2EUSJ-GCcezGcKE";

    private String shortenedURL = "";
    private final static String REQUEST_QR_CODE_URL = "http://chart.googleapis.com/chart?cht=qr&chs=547x547&choe=UTF-8&chld=H&chl=";
    private String genQRcodeURL = "";

    DownloadImage downloadImage;
    Dialog dialog;
    Bitmap bitmap;
    ImageView img;
    Gson gson;
    ClipData clip;
    ClipboardManager clipboard;
    RequestData requestData;

    public TextView getResultTextView() {
        return resultTextView;
    }

    public void setResultTextView(String result) {
        this.resultTextView.setText(result);
    }

    public void getTargetEditText() {
        targetText = targetEditText.getText().toString();
    }

    public void setTargetEditText(String text) {
        this.targetEditText.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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
                    Toast.makeText(this, "Clipboard is empty.\nThere is nothing to paste", Toast.LENGTH_LONG).show();
                }

                if (!TextUtils.isEmpty(pasteText)) {
                    setTargetEditText(pasteText);
                }
            }

            break;
            case R.id.btnCopy: {
                if (id.startsWith("http")) {
                    clip = ClipData.newPlainText("text", id);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No results yet", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case R.id.btnSubmit: {
                getTargetEditText();
                //Verify field not empty
                if (!(targetText.matches(""))) {
                    //Verify internet connected
                    if (isOnline()) {
                        requestData = new RequestData();
                        requestData.setLongUrl(targetText);
                        setResultTextView("Waiting...");
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
                shortenedURL = resultTextView.getText().toString();
                //Check internet connection
                if (isOnline()) {
                    //Check URL present
                    if (shortenedURL.startsWith("http")) {
                        //Concatenate template with shortened URL
                        genQRcodeURL = REQUEST_QR_CODE_URL + shortenedURL;
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
                    id = obj.getString("id");
                    setResultTextView(id);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    setResultTextView("");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
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
}
