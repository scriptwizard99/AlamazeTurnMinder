package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class DisplayDatesActivity extends AppCompatActivity {

    private TextView text;
    private Button refreshButton;
    private Context me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_dates);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        text = (TextView) findViewById(R.id.displayDatesTextView);
        refreshButton = (Button) findViewById(R.id.refreshDatesButton);
        me = this;


        refreshButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {

                                                 ConnectivityManager connMgr = (ConnectivityManager)
                                                         getSystemService(Context.CONNECTIVITY_SERVICE);
                                                 NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                                 if (networkInfo != null && networkInfo.isConnected()) {
                                                     new InfoGetterTask(me,text).execute();
                                                 } else {
                                                     text.setText("No network connection available.");
                                                 }

                                             }


                                         });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }




} // end class DisplayDatesActivity
