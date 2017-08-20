package com.quickblox.sample.groupchatwebrtc.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.PreferenceUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static com.quickblox.sample.groupchatwebrtc.Links.IP;

public class LoginActivity extends AppCompatActivity {

    public final String fetchDataUrl = "http://"+IP+"/si.txt";
    private CoordinatorLayout mLoginLayout;
    private TextInputEditText mUserIdConnectEditText, mUserNicknameEditText;
    private Button mConnectButton;
    private ContentLoadingProgressBar mProgressBar;
    public static String groupMessage = "";
    public static String reg_name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        SendBird.init("E236FA3C-C3D8-49D6-A0A0-5ED01BC4BCF6",getApplicationContext());

        new tryFetchingData().execute(fetchDataUrl);

        mLoginLayout = (CoordinatorLayout) findViewById(R.id.layout_login);

        mUserIdConnectEditText = (TextInputEditText) findViewById(R.id.edittext_login_user_id);
        mUserNicknameEditText = (TextInputEditText) findViewById(R.id.edittext_login_user_nickname);

        mUserIdConnectEditText.setText(PreferenceUtils.getUserId(this));
        mUserNicknameEditText.setText(PreferenceUtils.getNickname(this));

        mConnectButton = (Button) findViewById(R.id.button_login_connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //     Toast.makeText(getApplicationContext(),mUserNicknameEditText.getText().toString()+"^"+mUserIdConnectEditText.getText().toString()+"User does not Exists",Toast.LENGTH_LONG).show();
            //    Toast.makeText(getApplicationContext(),groupMessage,Toast.LENGTH_LONG).show();

                if (!groupMessage.contains(mUserNicknameEditText.getText().toString()+"^"+mUserIdConnectEditText.getText().toString())) {

                    Toast.makeText(getApplicationContext(),"User does not Exists",Toast.LENGTH_LONG).show();
                }
                else {
                }
                    String userId = mUserIdConnectEditText.getText().toString();
                    // Remove all spaces from userID
                    userId = userId.replaceAll("\\s", "");

                    String userNickname = mUserNicknameEditText.getText().toString();

                    PreferenceUtils.setUserId(LoginActivity.this, userId);
                    PreferenceUtils.setNickname(LoginActivity.this, userNickname);

                    connectToSendBird(userId, userNickname);



            }
        });

        // A loading indicator
        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.progress_bar_login);

        // Display current SendBird and app versions in a TextView
        String sdkVersion = String.format(getResources().getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion());
        ((TextView) findViewById(R.id.text_login_versions)).setText(sdkVersion);
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
            Toast.makeText(getApplicationContext(),"data loades",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());

        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if(PreferenceUtils.getConnected(this)) {
            connectToSendBird(PreferenceUtils.getUserId(this), PreferenceUtils.getNickname(this));
        }
    }

    /**
     * Attempts to connect a user to SendBird.
     * @param userId    The unique ID of the user.
     * @param userNickname  The user's nickname, which will be displayed in chats.
     */
    private void connectToSendBird(final String userId, final String userNickname) {
        // Show the loading indicator
        showProgressBar(true);
        mConnectButton.setEnabled(false);
        try {
            SendBird.connect(userId, new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    // Callback received; hide the progress bar.
                    showProgressBar(false);

                    if (e != null) {
                        // Error!
                        Toast.makeText(
                                LoginActivity.this, "" + e.getCode() + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();

                        // Show login failure snackbar
                        showSnackbar("Login to SendBird failed");
                        mConnectButton.setEnabled(true);
                        PreferenceUtils.setConnected(LoginActivity.this, false);
                        return;
                    }

                    PreferenceUtils.setConnected(LoginActivity.this, true);

                    // Update the user's nickname
                    updateCurrentUserInfo(userNickname);
                    // updateCurrentUserPushToken();

                    // Proceed to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });




        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Update the user's push token.
     */


    /**
     * Updates the user's nickname.
     * @param userNickname  The new nickname of the user.
     */
    private void updateCurrentUserInfo(String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(
                            LoginActivity.this, "" + e.getCode() + ":" + e.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();

                    // Show update failed snackbar
                    showSnackbar("Update user nickname failed");

                    return;
                }

            }
        });
    }

    // Displays a Snackbar from the bottom of the screen
    private void showSnackbar(String text) {
        Snackbar snackbar = Snackbar.make(mLoginLayout, text, Snackbar.LENGTH_SHORT);

        snackbar.show();
    }

    // Shows or hides the ProgressBar
    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.show();
        } else {
            mProgressBar.hide();
        }
    }

    public void sup(View view) {

        Intent init=new Intent(LoginActivity.this,SignUp.class);
        startActivity(init);

    }
}
