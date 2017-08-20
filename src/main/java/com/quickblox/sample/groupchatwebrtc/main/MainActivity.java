package com.quickblox.sample.groupchatwebrtc.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.sample.groupchatwebrtc.activities.ActivityDraw;
import com.quickblox.sample.groupchatwebrtc.activities.SplashActivity;
import com.quickblox.sample.groupchatwebrtc.groupchannel.GroupChatFragment;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.Record;
import com.quickblox.sample.groupchatwebrtc.groupchannel.GroupChannelActivity;
import com.quickblox.sample.groupchatwebrtc.openchannel.OpenChannelActivity;
import com.quickblox.sample.groupchatwebrtc.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private NavigationView mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);



        mNavView = (NavigationView) findViewById(R.id.nav_view_main);
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_item_open_channels) {
                    Intent intent = new Intent(MainActivity.this, OpenChannelActivity.class);
                    startActivity(intent);
                    return true;

                } else if (id == R.id.nav_item_group_channels) {
                    Intent intent = new Intent(MainActivity.this, GroupChannelActivity.class);
                    startActivity(intent);
                    return true;

                } else if (id == R.id.nav_item_disconnect) {
                    // Unregister push tokens and disconnect
                    disconnect();
                    return true;
                }
                else if (id == R.id.record) {
                    // Unregister push tokens and disconnect
                    Intent intent = new Intent(MainActivity.this, Record.class);
                    startActivity(intent);
                    return true;
                }
                else if (id == R.id.call) {
                    // Unregister push tokens and disconnect
                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (id == R.id.books) {
                    GroupChannelActivity.file="yes";
                    Intent intent = new Intent(MainActivity.this, GroupChannelActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (id == R.id.recommendation) {
                    recommendation();
                    return true;
                }



                return false;
            }
        });

        // Displays the SDK version in a TextView
        String sdkVersion = String.format(getResources().getString(R.string.all_app_version),
                BaseApplication.VERSION, SendBird.getSDKVersion());
        ((TextView) findViewById(R.id.text_main_versions)).setText(sdkVersion);
    }
    void recommendation(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(" Recommendation");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);


            input.setHint(preferences.getString("recommendation", null));


        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_save_black_24dp);

        alertDialog.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("recommendation", input.getText().toString());
                        editor.commit();


                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.setNeutralButton("Clear",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("recommendation", "");
                        editor.commit();
                    }
                });

        alertDialog.show();
    }
    /**
     * Unregisters all push tokens for the current user so that they do not receive any notifications,
     * then disconnects from SendBird.
     */
    private void disconnect() {
        SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
            @Override
            public void onUnregistered(SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();

                    // Don't return because we still need to disconnect.
                } else {
                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT)
                            .show();
                }

                SendBird.disconnect(new SendBird.DisconnectHandler() {
                    @Override
                    public void onDisconnected() {
                        PreferenceUtils.setConnected(MainActivity.this, false);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
}
