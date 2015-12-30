package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * The purpose of this activity is to get the login and
 * password that players use for the Order Checker page.
 * The next activity will verify that info.
 */

public class OCLoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Context me;

    public final static String EXTRA_USERID = "com.scriptwizard.alamazeturnminder.userID";
    public final static String EXTRA_PASSID = "com.scriptwizard.alamazeturnminder.passID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oclogin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        me = this;
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView user = (TextView) findViewById(R.id.ocLogin);
                String userStr = String.valueOf(user.getText());
                TextView pw = (TextView) findViewById(R.id.ocPassword);
                String pwStr = String.valueOf(pw.getText());

                Intent intent = new Intent(me, RefreshGamesActivity.class);
                intent.putExtra(EXTRA_USERID, userStr);
                intent.putExtra(EXTRA_PASSID, pwStr);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
