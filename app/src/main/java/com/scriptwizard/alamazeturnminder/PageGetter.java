package com.scriptwizard.alamazeturnminder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jgibbs on 12/17/15.
 */
public class PageGetter {

    Context mContext;

    PageGetter(Context mContext) {
        this.mContext = mContext;
    };

      /**
     * Will throw if not connected
     */
    private void assertConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new RuntimeException("Network not found!");
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500000;

        Log.d("DOWNLOAD","Fetching data from: "+myurl);
        try {
            assertConnected();
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            //conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            len = conn.getContentLength();
            Log.d("DOWNLOAD", "The response is: " + response +"  Length="+len);
            is = conn.getInputStream();


            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

        } catch (Exception e) {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            e.printStackTrace();
            return "Caught exception: " + e.getMessage();
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len+1];
        int bytesRead= 0;
        int bytesLeft = len;
        int bytesTotal = 0;
        while (bytesTotal < len ) {
            //Log.d("READIT", "br="+bytesRead+ " bt=" + bytesTotal + " bl=" + bytesLeft);
            bytesRead = reader.read(buffer, bytesTotal, bytesLeft);
            bytesTotal += bytesRead;
            bytesLeft -= bytesRead;
        }

        return new String(buffer);
    }


} // end class PageGetter
