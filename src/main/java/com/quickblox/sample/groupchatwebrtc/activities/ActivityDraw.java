package com.quickblox.sample.groupchatwebrtc.activities;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.sample.groupchatwebrtc.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ActivityDraw extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        PathRedoUndoCountChangeListener, FreeDrawView.DrawCreatorListener, PathDrawnListener {

    private static final String TAG = ActivityDraw.class.getSimpleName();

    private static final int THICKNESS_STEP = 2;
    private static final int THICKNESS_MAX = 80;
    private static final int THICKNESS_MIN = 15;

    private static final int ALPHA_STEP = 1;
    private static final int ALPHA_MAX = 255;
    private static final int ALPHA_MIN = 0;
    // public static String IP="cardoctoronline.com";
    public static String IP="192.168.1.4";


    public static String file_url = "http://"+IP+"/draw_state.ser.png";

    public final String sendDataUrl = "http://"+IP+"/putData.php";
    public final String fetchDataUrl = "http://"+IP+"/messages.txt";




    public final String sendchkUrl = "http://"+IP+"/chk.php";
    public final String fetchchkUrl = "http://"+IP+"/chk.txt";
    public static String chksend = "";
    public static String chkstatus = "";



    public final int updateFrequency = 500; // In milli seconds


    public static String groupMessage = "No messages as of yet !";
    public static String userMessage = "";


    int serverResponseCode = 0;


    String upLoadServerUri = null;

    String uploadFilePath ;
    final String uploadFileName = "draw_state.ser";

    private LinearLayout mRoot;
    private FreeDrawView mFreeDrawView;
    private View mSideView;
    private Button mBtnRandomColor, mBtnUndo, mBtnRedo, mBtnClearAll;
    private SeekBar mThicknessBar, mAlphaBar;
    private TextView mTxtRedoCount, mTxtUndoCount;
    private ProgressBar mProgressBar;

    private ImageView mImgScreen;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);



        File rootc = android.os.Environment.getExternalStorageDirectory();

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        uploadFilePath= getFilesDir().getAbsolutePath() ;


        mRoot = (LinearLayout) findViewById(R.id.root);

        mImgScreen = (ImageView) findViewById(R.id.img_screen);

        mTxtRedoCount = (TextView) findViewById(R.id.txt_redo_count);
        mTxtUndoCount = (TextView) findViewById(R.id.txt_undo_count);

        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        mFreeDrawView = (FreeDrawView) findViewById(R.id.free_draw_view);
        mFreeDrawView.setOnPathDrawnListener(this);
        mFreeDrawView.setPathRedoUndoCountChangeListener(this);





        /************* Php script path ****************/





        /*
        mFreeDrawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_UP){

                    Toast.makeText(getApplicationContext(),"up",Toast.LENGTH_LONG).show();


                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN){


                     Toast.makeText(getApplicationContext(),"down",Toast.LENGTH_LONG).show();


                    return true;
                }
                return false;
            }
        });
*/
        mSideView = findViewById(R.id.side_view);
        mBtnRandomColor = (Button) findViewById(R.id.btn_color);
        mBtnUndo = (Button) findViewById(R.id.btn_undo);
        mBtnRedo = (Button) findViewById(R.id.btn_redo);
        mBtnClearAll = (Button) findViewById(R.id.btn_clear_all);
        mAlphaBar = (SeekBar) findViewById(R.id.slider_alpha);
        mThicknessBar = (SeekBar) findViewById(R.id.slider_thickness);

        mAlphaBar.setOnSeekBarChangeListener(null);
        mThicknessBar.setOnSeekBarChangeListener(null);

        mBtnRandomColor.setOnClickListener(this);
        mBtnUndo.setOnClickListener(this);
        mBtnRedo.setOnClickListener(this);
        mBtnClearAll.setOnClickListener(this);



        mAlphaBar.setMax((ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP);
        int alphaProgress = ((mFreeDrawView.getPaintAlpha() - ALPHA_MIN) / ALPHA_STEP);
        mAlphaBar.setProgress(alphaProgress);
        mAlphaBar.setOnSeekBarChangeListener(this);

        mThicknessBar.setMax((THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP);
        int thicknessProgress = (int)
                ((mFreeDrawView.getPaintWidth() - THICKNESS_MIN) / THICKNESS_STEP);
        mThicknessBar.setProgress(thicknessProgress);
        mThicknessBar.setOnSeekBarChangeListener(this);
        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());












        final EditText userMessageBox=(EditText)findViewById(R.id.userMessageBox);
        userMessageBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                userMessage=userMessageBox.getText().toString();
                new trySendingData().execute(sendDataUrl);
            }

        });
        // start group message sync
        new tryFetchingData().execute(fetchDataUrl);


        new tryFetchingchk().execute(fetchchkUrl);

        new DownloadFileFromURL().execute(file_url);


    }



    // Asynchronous thread for message updation..
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
            // Update content to the UI..
            updateMessages();

            // Wait before trying for next update..
            Handler myHandler = new Handler();
            myHandler.postDelayed(delayedUpdateLooper, updateFrequency);
        }
    }

    // Asynchronous thread for user message sending..
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

        }
    }

    private Runnable delayedUpdateLooper = new Runnable() {
        @Override
        public void run() {
            new tryFetchingData().execute(fetchDataUrl);

        }
    };



    public void updateMessages() {
        TextView groupMessageBox = (TextView) this
                .findViewById(R.id.groupMessageBox);
        groupMessageBox.setText(groupMessage);

    }

    // Send data to server
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

    // Fetching data from server
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





















    // Asynchronous thread for message updation..
    private class tryFetchingchk extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... url) {
            getDataFromchk(url[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Long result) {
            // Update content to the UI..
            updateMessages();

            // Wait before trying for next update..
            Handler myHandler = new Handler();
            myHandler.postDelayed(delayedUpdateLooperchk, updateFrequency);
        }
    }

    // Asynchronous thread for user message sending..
    private class trySendingchk extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... url) {
            sendDataTochk(url[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected void onPostExecute(Long result) {

        }
    }

    private Runnable delayedUpdateLooperchk = new Runnable() {
        @Override
        public void run() {

            new tryFetchingchk().execute(fetchchkUrl);

            if(chkstatus.contains("yes")){
                damn();
            }

        }
    };




    // Send data to server
    public void sendDataTochk(String url) {
        // Making HTTP POST request to send data
        try {
            // A client to do a HTTP Post request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            // Adding nameValuePairs - message as a post variable to the request
            List<NameValuePair> msg = new ArrayList<NameValuePair>();
            msg.add(new BasicNameValuePair("msg",chksend));
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

    // Fetching data from server
    public void getDataFromchk(String url) {
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
                chkstatus = serverResponse;
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());

        }
    }







    private void hideLoadingSpinner() {

        mProgressBar.setVisibility(View.GONE);
    }




    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void changeColor() {

        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());
        int color = ColorHelper.getRandomMaterialColor(this);

        mFreeDrawView.setPaintColor(color);

        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == mBtnRandomColor.getId()) {
            changeColor();
        }

        if (id == mBtnUndo.getId()) {
            mFreeDrawView.undoLast();
        }

        if (id == mBtnRedo.getId()) {
            mFreeDrawView.redoLast();
        }

        if (id == mBtnClearAll.getId()) {
            mFreeDrawView.undoAll();
        }
    }

    // SliderListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == mThicknessBar.getId()) {
            mFreeDrawView.setPaintWidthPx(THICKNESS_MIN + (progress * THICKNESS_STEP));
        } else {
            mFreeDrawView.setPaintAlpha(ALPHA_MIN + (progress * ALPHA_STEP));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onBackPressed() {
        if (mImgScreen.getVisibility() == View.VISIBLE) {
            mMenu.findItem(R.id.menu_screen).setVisible(true);
            mMenu.findItem(R.id.menu_delete).setVisible(true);
            mImgScreen.setImageBitmap(null);
            mImgScreen.setVisibility(View.GONE);

            mFreeDrawView.setVisibility(View.VISIBLE);
            mSideView.setVisibility(View.VISIBLE);

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            super.onBackPressed();
        }
    }

    // PathRedoUndoCountChangeListener.
    @Override
    public void onUndoCountChanged(int undoCount) {
        mTxtUndoCount.setText(String.valueOf(undoCount));
    }

    @Override
    public void onRedoCountChanged(int redoCount) {
        mTxtRedoCount.setText(String.valueOf(redoCount));
    }


    // PathDrawnListener
    @Override
    public void onNewPathDrawn() {
        // The user has finished drawing a path
        //  Toast.makeText(getApplicationContext(),"finisged stroke",Toast.LENGTH_LONG).show();

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        //  Toast.makeText(getApplicationContext(),getFilesDir().getAbsolutePath() ,Toast.LENGTH_SHORT).show();



        FreeDrawView view = (FreeDrawView) findViewById(R.id.free_draw_view);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        try {
            FileOutputStream output = new FileOutputStream(uploadFilePath + "/" + uploadFileName+".png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileHelper.saveStateIntoFile(this, mFreeDrawView.getCurrentViewStateAsSerializable(), null);
        upLoadServerUri = "http://"+IP+"/UploadToServer.php";
        //   uploadFile(uploadFilePath + "" + uploadFileName);

        chksend="yes";
        new trySendingchk().execute(sendchkUrl);



        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {

                    }
                });

                uploadFile(uploadFilePath + "/" + uploadFileName+".png");

            }
        }).start();
    }




    @Override
    public void onPathStart() {
        // The user has started drawing a path
    }


    // DrawCreatorListener
    @Override
    public void onDrawCreated(Bitmap draw) {
        mSideView.setVisibility(View.GONE);
        mFreeDrawView.setVisibility(View.GONE);

        mMenu.findItem(R.id.menu_screen).setVisible(false);
        mMenu.findItem(R.id.menu_delete).setVisible(false);

        mImgScreen.setVisibility(View.VISIBLE);

        mImgScreen.setImageBitmap(draw);
    }

    @Override
    public void onDrawCreationError() {
        Toast.makeText(this, "Error, cannot create bitmap", Toast.LENGTH_SHORT).show();
    }





    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {



            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + uploadFileName);

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),"Source File not exist :"
                            +uploadFilePath + "" + uploadFileName,Toast.LENGTH_LONG);
                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=uploaded_file;filename="+ fileName + "" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {




                            Toast.makeText(getApplicationContext(), "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {


                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {


                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }

            return serverResponseCode;

        } // End else block
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            FileHelper.deleteSavedStateFile(getApplicationContext());


        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream

                FileHelper.deleteSavedStateFile(getApplicationContext());
                OutputStream output = new FileOutputStream(getFilesDir().getAbsolutePath()+ "/draw_state.ser.png");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded

            chksend="no";
            new trySendingchk().execute(sendchkUrl);



            Toast.makeText(getApplicationContext(),"load complete",Toast.LENGTH_SHORT).show();
            File imgFile = new  File(uploadFilePath+"/"+uploadFileName+".png");

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                ImageView myImage = (ImageView) findViewById(R.id.img_screen);

                myImage.setImageBitmap(myBitmap);

            }

            FileHelper.getSavedStoreFromFile(getApplicationContext(),
                    new FileHelper.StateExtractorInterface() {
                        @Override
                        public void onStateExtracted(FreeDrawSerializableState state) {
                            if (state != null) {

                                mFreeDrawView.restoreStateFromSerializable(state);
                                //   Toast.makeText(getApplicationContext(),"load complete",Toast.LENGTH_SHORT).show();


                            }

                            hideLoadingSpinner();
                        }

                        @Override
                        public void onStateExtractionError() {
                            hideLoadingSpinner();
                        }
                    });

        }

    }





    private void damn() {

        new DownloadFileFromURL().execute(file_url);


    }
}
