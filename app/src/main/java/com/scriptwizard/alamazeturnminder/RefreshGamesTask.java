package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
        List<String> gameList = null;
        outString.append("\nChecking against information from the Alamaze site...\n\n");

        try {
            gameList = getUserGames(pg);

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

        updateSavedGameList(gameList);

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
    } // end getUserGames

    private void updateSavedGameList(List<String> newGameList) {


        Map<String,String> oldGameHash = getSavedGameMap();
        Map<String,String> newGameHash = new HashMap<String,String>();
        StringBuilder dataString = new StringBuilder();



        /*
         * Create new save game string from the new game list.
         * If one of those games exists in the old game hash,
         * then use the turn number from that hash. Otherwise,
         * set it to turn 1.
         */
        for(String gameKey : newGameList) {
            String turnNum = "1";
            if( oldGameHash.containsKey(gameKey)) {
                turnNum = oldGameHash.get(gameKey);
            }

            dataString.append(gameKey+"-"+turnNum+",");
        }

        /*
         * Write the new list to preferences as a
         * comma separated list.
         */
        try {
            FileOutputStream outputStream = mContext.openFileOutput(mContext.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE);
            Log.d("updateSavedGameList", "DataString=["+dataString.toString()+"]");
            outputStream.write(dataString.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    } // end updateSavedGameList

    /**
     * Games are saved in the form "game:kingdom-turn"
     * @return Hash of "game:kingdom"->"turn"
     */
    private Map<String,String> getSavedGameMap() {
        Map<String,String> gameHash = new HashMap<String,String>();

        FileInputStream inFile;
        String gameStr;
        try {
            inFile = mContext.openFileInput(mContext.getString(R.string.preference_file_key));
            Scanner scanner = new Scanner(inFile);
            scanner.useDelimiter("\\A");
            gameStr = scanner.next();
        } catch (FileNotFoundException e) {
            gameStr = "";
        }

        //String gameStr = sharedPref.getString(mContext.getString(R.string.game_list_key), "");
        String[] instances = gameStr.split(",");
        for(String instance: instances) {
            String[] pieces = instance.split("-");
            if(pieces.length != 2) continue;
            gameHash.put(pieces[0], pieces[1]);
        }
        return gameHash;
    }

} // end class com.scriptwizard.alamazeturnminder.InfoGetterTask
