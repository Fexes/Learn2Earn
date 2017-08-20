package com.quickblox.sample.groupchatwebrtc.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.sample.groupchatwebrtc.fragments.AudioConversationFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.BaseConversationFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.ConversationFragmentCallbackListener;
import com.quickblox.sample.groupchatwebrtc.fragments.IncomeCallFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.IncomeCallFragmentCallbackListener;
import com.quickblox.sample.groupchatwebrtc.fragments.OnCallEventsController;
import com.quickblox.sample.groupchatwebrtc.fragments.ScreenShareFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.VideoConversationFragment;
import com.quickblox.sample.groupchatwebrtc.util.NetworkConnectionChecker;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.FragmentExecuotr;
import com.quickblox.sample.groupchatwebrtc.utils.PermissionsChecker;
import com.quickblox.sample.groupchatwebrtc.utils.QBEntityCallbackImpl;
import com.quickblox.sample.groupchatwebrtc.utils.RingtonePlayer;
import com.quickblox.sample.groupchatwebrtc.utils.SettingsUtil;
import com.quickblox.sample.groupchatwebrtc.utils.UsersUtils;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.quickblox.sample.groupchatwebrtc.Links.IP;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements QBRTCClientSessionCallbacks, QBRTCSessionStateCallback, QBRTCSignalingCallback,
        OnCallEventsController, IncomeCallFragmentCallbackListener, ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener, ScreenShareFragment.OnSharingEvents,View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        PathRedoUndoCountChangeListener, FreeDrawView.DrawCreatorListener, PathDrawnListener {















    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private QBRTCSession currentSession;
    public List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBRTCClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private ConnectionListener connectionListener;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    private RingtonePlayer ringtonePlayer;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private List<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean isVideoCall;
    private long expirationReconnectionTime;
    private int reconnectHangUpTimeMillis;
    private boolean headsetPlugged;
    private boolean previousDeviceEarPiece;
    private boolean showToastAfterHeadsetPlugged = true;
    private PermissionsChecker checker;
    private MediaProjectionManager mMediaProjectionManager;

    public static void start(Context context,
                             boolean isIncomingCall) {

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);

        context.startActivity(intent);
    }








    private static final int THICKNESS_STEP = 2;
    private static final int THICKNESS_MAX = 80;
    private static final int THICKNESS_MIN = 15;

    private static final int ALPHA_STEP = 1;
    private static final int ALPHA_MAX = 255;
    private static final int ALPHA_MIN = 0;
    // public static String IP="cardoctoronline.com";


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
        setContentView(R.layout.activity_main);

        parseIntentExtras();

        sessionManager = WebRtcSessionManager.getInstance(this);
        if (!currentSessionExist()) {
//            we have already currentSession == null, so it's no reason to do further initialization
            finish();
            Log.d(TAG, "finish CallActivity");
            return;
        }

        initFields();
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initQBRTCClient();
        initAudioManager();
        initWiFiManagerListener();

        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);
        checker = new PermissionsChecker(getApplicationContext());

        startSuitableFragment(isInCommingCall);








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
    private void startScreenSharing(final Intent data){
        ScreenShareFragment screenShareFragment = ScreenShareFragment.newIntstance();
        FragmentExecuotr.addFragmentWithBackStack(getSupportFragmentManager(), R.id.fragment_container, screenShareFragment, ScreenShareFragment.TAG);
        currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
    }

    private void returnToCamera() {
        try {
            currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCCameraVideoCapturer(this, null));
        } catch (QBRTCCameraVideoCapturer.QBRTCCameraCapturerException e) {
            Log.i(TAG, "Error: device doesn't have camera");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,final Intent data) {
        Log.i(TAG, "onActivityResult requestCode="+requestCode +", resultCode= " + resultCode);
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data);
                Log.i(TAG, "Starting screen capture");
            }
            else {

            }
        }
    }

    private void startSuitableFragment(boolean isInComingCall) {
        if (isInComingCall) {
            initIncomingCallTask();
            startLoadAbsentUsers();
            addIncomeCallFragment();
            checkPermission();
        } else {
            addConversationFragment(isInComingCall);
        }
    }

    private void checkPermission() {
        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
            startPermissionsActivity(!isVideoCall);
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();
        allParticipantsOfCall.addAll(opponentsIdsList);

        if (isInCommingCall) {
            allParticipantsOfCall.add(currentSession.getCallerID());
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                    dbManager.saveAllUsers(result, false);
                    needUpdateOpponentsList(result);
                }
            });
        }
    }

    private void needUpdateOpponentsList(ArrayList<QBUser> newUsers) {
        notifyCallStateListenersNeedUpdateOpponentsList(newUsers);
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    private void initFields() {
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        opponentsIdsList = currentSession.getOpponents();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void parseIntentExtras() {
        isInCommingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                if (callStarted) {
                    if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                        previousDeviceEarPiece = true;
                    } else if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                        previousDeviceEarPiece = false;
                    }
                    if (showToastAfterHeadsetPlugged) {
                        Toaster.shortToast("Audio device switched to  " + audioDevice);
                    }
                }
            }
        });

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                headsetPlugged = plugged;
                if (callStarted) {
                    Toaster.shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
                }
                if (onChangeDynamicCallback != null) {
                    if (!plugged) {
                        showToastAfterHeadsetPlugged = false;
                        if (previousDeviceEarPiece) {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                        } else {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        }
                    }
                    onChangeDynamicCallback.enableDynamicToggle(plugged, previousDeviceEarPiece);
                }
            }
        });
        audioManager.init();
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastAfterHeadsetPlugged = true;
                audioManager.setAudioDevice(audioDevice);
            }
        }, 500);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);

        rtcClient.setCameraErrorHendler(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {

                showToast("Camera error: " + s);
            }

            @Override
            public void onCameraDisconnected() {
                showToast("Camera onCameraDisconnected: ");
            }

            @Override
            public void onCameraFreezed(String s) {
                showToast("Camera freezed: " + s);
                hangUpCurrentSession();
            }

            @Override
            public void onCameraOpening(String s) {
                showToast("Camera aOpening: " + s);
            }

            @Override
            public void onFirstFrameAvailable() {
                showToast("onFirstFrameAvailable: ");
            }

            @Override
            public void onCameraClosed() {
            }
        });


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
        SettingsUtil.configRTCTimers(CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();
        connectionListener = new ConnectionListener();
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    private void setExpirationReconnectionTime() {
        reconnectHangUpTimeMillis = SettingsUtil.getPreferenceInt(sharedPref, this, R.string.pref_disconnect_time_interval_key,
                R.string.pref_disconnect_time_interval_default_value) * 1000;
        expirationReconnectionTime = System.currentTimeMillis() + reconnectHangUpTimeMillis;
    }

    private void hangUpAfterLongReconnection() {
        if (expirationReconnectionTime < System.currentTimeMillis()) {
            hangUpCurrentSession();
        }
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        if (callStarted) {
            showToast("Internet connection " + (availableNow ? "available" : " unavailable"));
        }
    }

    private void showNotificationPopUp(final int text, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ((TextView) connectionView.findViewById(R.id.notification)).setText(text);
                    if (connectionView.getParent() == null) {
                        ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
                    }
                } else {
                    ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
                }
            }
        });

    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentSession == null) {
                    return;
                }

                QBRTCSession.QBRTCSessionState currentSessionState = currentSession.getState();
                if (QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW.equals(currentSessionState)) {
                    rejectCurrentSession();
                } else {
                    ringtonePlayer.stop();
                    hangUpCurrentSession();
                }
                Toaster.longToast("Call was stopped by timer");
            }
        };
    }


    private QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    public void hangUpCurrentSession() {
        ringtonePlayer.stop();
        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());
        }
    }

    private void setAudioEnabled(boolean isAudioEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isAudioEnabled);
        }
    }

    private void setVideoEnabled(boolean isVideoEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(isVideoEnabled);
        }
    }

    private void startIncomeCallTimer(long time) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }


    private void forbiddenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void initCurrentSession(QBRTCSession session) {
        if (session != null) {
            Log.d(TAG, "Init new QBRTCSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addSignalingCallback(CallActivity.this);
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeSignalingCallback(CallActivity.this);
            rtcClient.removeSessionsCallbacksListener(CallActivity.this);
            this.currentSession = null;
        }
    }

    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " are income");
        if (getCurrentSession() != null) {
            Log.d(TAG, "Stop new session. Device now is busy");
            session.rejectCall(null);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        callStarted = true;
        notifyCallStateListenersCallStarted();
        forbiddenCloseByWifiState();
        if (isInCommingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {

        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(getCurrentSession())) {
            Log.d(TAG, "Stop session");

            if (audioManager != null) {
                audioManager.close();
            }
            releaseCurrentSession();

            closeByWifiStateAllow = true;
            finish();
        }
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (session.equals(getCurrentSession())) {
            session.removeSessionCallbacksListener(CallActivity.this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {

    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (session.equals(getCurrentSession())) {

            if (userID.equals(session.getCallerID())) {
                hangUpCurrentSession();
                Log.d(TAG, "initiator hung up the call");
            }

            QBUser participant = dbManager.getUserById(userID);
            final String participantName = participant != null ? participant.getFullName() : String.valueOf(userID);

            showToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation");
        }
    }

    private android.support.v4.app.Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + currentSession);

        if (currentSession != null) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    private void addConversationFragment(boolean isIncomingCall) {
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(
                isVideoCall
                        ? new VideoConversationFragment()
                        : new AudioConversationFragment(),
                isIncomingCall);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId, QBRTCSignalException e) {
        showToast(R.string.dlg_signal_error);
    }


    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    public void sendHeadsetState() {
        if (isInCommingCall) {
            onChangeDynamicCallback.enableDynamicToggle(headsetPlugged, previousDeviceEarPiece);
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    @Override
    public void onAcceptCurrentSession() {
        if (currentSession != null) {
            addConversationFragment(true);
        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        rejectCurrentSession();
    }
    //////////////////////////////////////////   end   /////////////////////////////////////////////




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).addSessionCallbacksListener(eventsCallback);
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onHangUpCurrentSession() {
        hangUpCurrentSession();
    }

    @TargetApi(21)
    @Override
    public void onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        QBRTCScreenCapturer.requestPermissions(CallActivity.this);
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        ((QBRTCCameraVideoCapturer)(currentSession.getMediaStreamManager().getVideoCapturer()))
                .switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void removeRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).removeSessionsCallbacksListener(eventsCallback);
    }

    @Override
    public void addCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.add(currentCallStateCallback);
    }

    @Override
    public void removeCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = onChangeDynamicCallback;
        sendHeadsetState();
    }

    @Override
    public void removeOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = null;
    }

    @Override
    public void onStopPreview() {
        onBackPressed();
    }

    //////////////////////////////////////////   end   /////////////////////////////////////////////
    private class ConnectionListener extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            showNotificationPopUp(R.string.connection_was_lost, true);
            setExpirationReconnectionTime();
        }

        @Override
        public void reconnectionSuccessful() {
            showNotificationPopUp(R.string.connection_was_lost, false);
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.i(TAG, "reconnectingIn " + seconds);
            if (!callStarted) {
                hangUpAfterLongReconnection();
            }
        }
    }

    public interface OnChangeDynamicToggle {
        void enableDynamicToggle(boolean plugged, boolean wasEarpiece);
    }


    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void notifyCallStateListenersCallStopped() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }
}