package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class RefreshGamesActivity extends AppCompatActivity {

    private String userID;
    private String passID;
    private Context me;
    private TextView text;
    private List<String> gameList;
    private RefreshGamesTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_games);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text = (TextView) findViewById(R.id.gameListTextView);
        me = this;

        Intent intent = getIntent();
        userID = intent.getStringExtra(OCLoginActivity.EXTRA_USERID);
        passID = intent.getStringExtra(OCLoginActivity.EXTRA_PASSID);

    } // end onCreate

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            task = new RefreshGamesTask(me, text, userID, passID);
            task.execute();
        } else {
            text.setText(getString(R.string.no_network_connection));
        }

    } // end onStart


}
