package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgibbs on 12/19/15.
 */
public class RefreshGamesTask extends AsyncTask<String, Void, String> {


    private StringBuilder outString;

    Context mContext;
    TextView text;
    PageGetter pg;
    private String userID;
    private String passID;

    RefreshGamesTask(Context mContext, TextView text, String userID, String passID) {
        this.mContext = mContext;
        this.text = text;
        this.userID = userID;
        this.passID = passID;
        outString = new StringBuilder();
    };

    @Override
    protected String doInBackground(String... params) {

        pg = new PageGetter(mContext);

        try {
            outString.append("\nChecking against information from the Alamaze site...\n\n");


            List<String> gameList = getUserGames(pg);

            if (gameList.isEmpty() ) {
                outString.append("Could not find any active games for you. Please double check login and password.");
            } else {
                outString.append("These are the games you are in:\n\n");
                for (String game : gameList) {
                    outString.append("\t"+game + "\n");
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            outString.append("Caught Exception: "+e.getMessage());
        }


        return outString.toString();
    } // end doInBackground


    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //TextView text = (TextView) findViewById(R.id.displayDatesTextView);
        text.setText(result);
    }


    private List<String> getUserGames(PageGetter pg) throws IOException {

        String url = mContext.getString(R.string.master_url);
        String userInfo = pg.downloadUrl(url);

        List gameList = new ArrayList<String>();
        boolean showData = false;
        boolean passMatch = false;
        boolean userMatch = false;
        String gameNum = "na";
        String userLine =  "USERNAME: " + userID;
        String passLine =  "PASSWORD: " + passID;


        BufferedReader reader = new BufferedReader(new StringReader(userInfo));
        String longLine = reader.readLine();
        while (longLine != null) {
            String line = longLine.trim();

            if (showData) {
                Log.i("getUserGames", "Found matching userID and pw");
                String[] pieces = line.split("\\s+");
                if ( pieces[0].equals("GAME") ) {
                    gameNum = pieces[2];
                }else if ( pieces[0].equals("KINGDOM")) {
                    gameList.add( gameNum + ":"+ pieces[2] );
                }
            } // end if showData

            if(line.equals(userLine) ) userMatch=true;
            if(line.equals(passLine) ) passMatch=true;
            if (userMatch && passMatch) showData = true;

            if (line.isEmpty() ) {
                showData = false;
                userMatch = false;
                passMatch = false;
            }


            longLine = reader.readLine();
        } // end while


        return gameList;
    }

} // end class com.scriptwizard.alamazeturnminder.InfoGetterTask
