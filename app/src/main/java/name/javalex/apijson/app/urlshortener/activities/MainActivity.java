package name.javalex.apijson.app.urlshortener.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import name.javalex.apijson.app.urlshortener.R;
import name.javalex.apijson.app.urlshortener.urlEntity.RequestData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public String id = "";
    private String pasteText = "http://stackoverflow.com/search?q=URL+Shortener+api+java";
    private String targetText = "";
    private TextView resultTextView;
    private EditText targetEditText;
    private final static String REQUEST_URL = "https://www.googleapis.com/urlshortener/v1/url?key=iwon'ttellyouthekey";

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

        Button shortenBtn = (Button) findViewById(R.id.btnSubmit);
        shortenBtn.setOnClickListener(this);

        Button copyBtn = (Button) findViewById(R.id.btnCopy);
        copyBtn.setOnClickListener(this);

        Button pasteBtn = (Button) findViewById(R.id.btnPaste);
        pasteBtn.setOnClickListener(this);

        targetEditText = (EditText)findViewById(R.id.editTextTargetUrl);
        resultTextView = (TextView) findViewById(R.id.textViewResult);

        clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

 }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
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
                    Log.e("Buffer data: ", getResultTextView().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No results yet", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case R.id.btnSubmit: {
                getTargetEditText();
                if (!(targetText.matches(""))) {
                    requestData = new RequestData();
                    requestData.setLongUrl(targetText);
                    setResultTextView("Waiting...");
                    try {
                        sendRequest(serialize(requestData));
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException err) {
                        Toast.makeText(this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                        err.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Please provide URL", Toast.LENGTH_SHORT).show();
                }

            }

            break;
        }
    }

    public String serialize(RequestData requestData) {
        gson = new Gson();
        return gson.toJson(requestData);
    }

    public void sendRequest(String body) throws JSONException, UnsupportedEncodingException, UnknownHostException{
            ByteArrayEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(getApplicationContext(), REQUEST_URL, entity, "application/json", new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                    Log.e("!!!!!!!!!!!!!! ", obj.toString());
                    try {
                        id = obj.getString("id");
                        setResultTextView(id);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Error Occured [Server response might be invalid]!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("!!!!!!!!!!!!!! ", "" + statusCode);
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Error 404\nNot Found", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Error 500\nServer Error", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 403) {
                        Toast.makeText(getApplicationContext(), "Error 403\nWrong data provided", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 400) {
                        Toast.makeText(getApplicationContext(), "Error 400\nBad Request", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 0) {
                        Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error " + statusCode + "\n" + throwable.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    }


}
