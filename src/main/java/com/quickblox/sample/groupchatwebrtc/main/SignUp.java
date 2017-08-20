package com.quickblox.sample.groupchatwebrtc.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.activities.ActivityDraw;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.quickblox.sample.groupchatwebrtc.Links.IP;


public class SignUp extends AppCompatActivity {

    public final String fetchDataUrl = "http://"+IP+"/si.txt";
     public final String sendDataUrl = "http://"+IP+"/si.php";
    String userMessage="";
    public final int updateFrequency = 500; // In milli seconds

    public static String groupMessage = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        new tryFetchingData().execute(fetchDataUrl);
    }
    private Runnable delayedUpdateLooper = new Runnable() {
        @Override
        public void run() {
            new tryFetchingData().execute(fetchDataUrl);

        }
    };

    public void signup(View view) {


        EditText ed=(EditText)findViewById(R.id.editText);
        EditText ed2=(EditText)findViewById(R.id.editText2);
        if (groupMessage.contains(ed.getText().toString()+"^")) {

            Toast.makeText(getApplicationContext(),"User Exists",Toast.LENGTH_LONG).show();
        }
        else {
            userMessage = ed.getText().toString() + "^" + ed2.getText().toString() + "--";

            new trySendingData().execute(sendDataUrl);
            ProgressBar pb=(ProgressBar)findViewById(R.id.progressBar2);
            pb.setVisibility(View.VISIBLE);
        }

    }
    private class tryFetchingData extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... url) {
            getDataFromServer(url[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Long result) {
            Handler myHandler = new Handler();
            myHandler.postDelayed(delayedUpdateLooper, updateFrequency);
        }
    }

    public void getDataFromServer(String url) {
        String serverResponse = null;
        InputStream is = null;
        try {
            // A client to get data from data
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpReq = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpReq);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            serverResponse = sb.toString();
            if (serverResponse != null)
                groupMessage = serverResponse;
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());

        }
    }

    private class trySendingData extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... url) {
            sendDataToServer(url[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected void onPostExecute(Long result) {


            Intent init=new Intent(SignUp.this,LoginActivity.class);
            startActivity(init);


        }
    }
    public void sendDataToServer(String url) {
        // Making HTTP POST request to send data
        try {
            // A client to do a HTTP Post request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            // Adding nameValuePairs - message as a post variable to the request
            List<NameValuePair> msg = new ArrayList<NameValuePair>();
            msg.add(new BasicNameValuePair("msg",userMessage));
            httpPost.setEntity(new UrlEncodedFormEntity(msg));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                httpEntity.consumeContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
