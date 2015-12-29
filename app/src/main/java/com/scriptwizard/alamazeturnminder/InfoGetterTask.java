package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.scriptwizard.alamazeturnminder.PageGetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by jgibbs on 12/19/15.
 */
public class InfoGetterTask extends AsyncTask<String, Void, String> {


        private StringBuilder outString;

    Context mContext;
    TextView text;
    PageGetter pg;

    InfoGetterTask(Context mContext, TextView text) {
        this.mContext = mContext;
        this.text = text;
        outString = new StringBuilder();
    };

    @Override
    protected String doInBackground(String... params) {

        pg = new PageGetter(mContext);

        try {
            outString.append("\nGathering information from the Alamaze site...\n\n");
            outString.append("Game Kingdom Turn Orders Influence DueDate\n");
            outString.append( getGameInfo("189", "TR"));
            outString.append( getGameInfo("190", "GN"));
            outString.append( getGameInfo("193", "GN"));

        } catch (IOException e) {
            e.printStackTrace();
            outString.append("Caught Exception: "+e.getMessage());
        }


        return outString.toString();
    } // end doInBackground

    private String getGameInfo(String game, String kingdom) throws IOException {
        String info=getInfo(game,kingdom);
        return String.format("%4s   %2s     %s", game, kingdom, info);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //TextView text = (TextView) findViewById(R.id.displayDatesTextView);
        text.setText(result);
    }


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
            Log.d("DUEDATE", x+" - "+line);
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

    private String getInfo(String game, String kingdom) throws IOException {
        String info="na";

        for(int turn=17; turn<=40; turn++) {
            int x = 0;
            x = getNumOrders(game, kingdom, String.valueOf(turn));
            String[] dueInfo = getDueDate(game, kingdom, String.valueOf(turn - 1));
            float influence = Float.parseFloat( dueInfo[1] );
            if (influence < 8.0 ) {
                return info;
            }
            info = String.format(" %3d  %3d    (%5s)  %s\n", turn, x, dueInfo[1], dueInfo[0]);
        }

        return info;
    }

} // end class com.scriptwizard.alamazeturnminder.InfoGetterTask
