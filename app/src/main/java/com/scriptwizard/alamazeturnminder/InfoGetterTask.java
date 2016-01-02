package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.scriptwizard.alamazeturnminder.PageGetter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

/**
 * Created by jgibbs on 12/19/15.
 */
public class InfoGetterTask extends AsyncTask<String, Void, String> {


    private StringBuilder outString;
    private StringBuilder saveStr;

    Context mContext;
    TextView text;
    PageGetter pg;
    private int tmpTurn;

    InfoGetterTask(Context mContext, TextView text) {
        this.mContext = mContext;
        this.text = text;
        this.outString = new StringBuilder();
        this.saveStr = new StringBuilder();
    };

    @Override
    protected String doInBackground(String... params) {

        pg = new PageGetter(mContext);

       FileInputStream inFile;
        String gameStr;
        try {
            inFile = mContext.openFileInput(mContext.getString(R.string.preference_file_key));
            Scanner scanner = new Scanner(inFile);
            scanner.useDelimiter("\\A");
            gameStr = scanner.next();
        } catch (FileNotFoundException e) {
            gameStr = "";
            Log.d("doInBackground", "Caught exception: " + e.getMessage());
        }

        if(gameStr.isEmpty()) {
           return "ERROR: No game list. Please refresh game list first.";
        }

        String[] instances = gameStr.split(",");
        Log.d("doInBackground", "gameStr=[" + gameStr + "] instances length=" + instances.length);

        try {
            //outString.append("\nGathering information from the Alamaze site...\n\n");
            outString.append("Game Kingdom Turn Orders Influence DueDate\n");

            for(String instance: instances) {
                String[] pieces = instance.split("-");
                Log.d("doInBackground", "pieces.length="+pieces.length);
                if(pieces.length != 2) continue;
                String startTurn = pieces[1];
                String[] pieces2 = pieces[0].split(":");
                if(pieces2.length != 2) continue;
                //gameHash.put(pieces[0], pieces[1]);
                outString.append( getGameInfo(pieces2[0], pieces2[1], startTurn));
            }

        } catch (IOException e) {
            e.printStackTrace();
            outString.append("Caught Exception: "+e.getMessage());
        }

        outString.append("\n\ndone.");

        return outString.toString();
    } // end doInBackground

    private String getGameInfo(String game, String kingdom, String startTurn) throws IOException {
        Log.d("getGameInfo", String.format("g(%s) k(%s) t(%s)", game, kingdom, startTurn));
        String info=getInfo(game,kingdom,startTurn);
        saveStr.append(String.format("%s:%s-%d,", game, kingdom, this.tmpTurn));
        return String.format("%4s   %2s     %s", game, kingdom, info);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //TextView text = (TextView) findViewById(R.id.displayDatesTextView);

        text.append(result);

        // Save the updated game info string.
        FileOutputStream outputStream = null;
        try {
            outputStream = mContext.openFileOutput(mContext.getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE);
            Log.d("onPostExecute", "DataString=["+saveStr.toString()+"]");
            outputStream.write(saveStr.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            text.append("\n\nError! Caught exception persisting data: " + e.getMessage());
        }
    } // end onPostExecute

    /**
     * Fetches the orders string from Alamaze and returns the number of orders issued.
     * @param game
     * @param kingdom
     * @param turn
     * @return
     * @throws IOException
     */
    private int getNumOrders(String game, String kingdom, String turn) throws IOException {
        int numOrders = 0;
        String urlBase = mContext.getString(R.string.orders_url);
        String url = String.format("%s/%s_%s.T%s", urlBase, game, kingdom, turn);
        String orders = pg.downloadUrl(url);
        numOrders = orders.trim().length() / 86;

        return numOrders;
    }

    private String[] getDueDate(String game, String kingdom, String turn) throws IOException {
        String dueDate="??";
        String  influence="0";
        String[] results = new String[2];
        String urlBase = mContext.getString(R.string.results_url);
        String url = String.format("%s/%s%s.R%s", urlBase, kingdom, game, turn);
        String resultPage = pg.downloadUrl(url);

        Log.d("DUEDATE", "page has " + resultPage.length() + " chars.\n");
        int x=0;
        BufferedReader reader = new BufferedReader(new StringReader(resultPage));
        String line = reader.readLine();
        while (line != null) {
            x += 1;
            //Log.d("DUEDATE", x+" - "+line);
            if (x == 38) {
                dueDate = line.trim();
            }

            if (line.matches("(.*)has current influence of(.*)") ) {
                int idx1 = line.indexOf("e of ") + 5;
                //int idx2 = line.indexOf(" ", idx1);
                influence = line.substring(idx1).trim();
                break;
            }
            line = reader.readLine();
        }

        results[0]=dueDate;
        results[1]=influence;

        return results;
    }

    /**
     * Also sets this.tmpTurn to the turn number that should be saved
     * along with this game:kingdom combo.
     * @param game
     * @param kingdom
     * @param startTurn
     * @return
     * @throws IOException
     */
    private String getInfo(String game, String kingdom, String startTurn) throws IOException {
        String info="na\n";

        Log.d("getInfo", String.format("startTurn str(%s) int(%d)", startTurn,Integer.parseInt(startTurn) ));

        for(int turn=Integer.parseInt(startTurn); turn<=40; turn++) {
            int x = 0;
            x = getNumOrders(game, kingdom, String.valueOf(turn));
            String[] dueInfo = getDueDate(game, kingdom, String.valueOf(turn - 1));
            float influence = Float.parseFloat( dueInfo[1] );
            if (influence < 8.0 ) {
                this.tmpTurn = turn -1;
                return info;
            }
            info = String.format(" %3d  %3d    (%5s)  %s\n", turn, x, dueInfo[1], dueInfo[0]);
        }

        return info;
    }

} // end class com.scriptwizard.alamazeturnminder.InfoGetterTask
