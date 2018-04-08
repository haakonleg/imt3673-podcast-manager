package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.parsers.PodcastParser;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;

public class ParsePodcastTask extends Task<Podcast> {
    private Context context;
    private String url;

    public ParsePodcastTask(Context context, String url, OnSuccessListener<Podcast> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.url = url;
    }

    @Override
    protected int doTask() {
        if (!CheckNetwork.hasNetwork(context)) {
            return ERROR_NO_INTERNET;
        }

        String xml;
        try {
            xml = download(this.url);
            if (xml == null) {
                return ERROR_DOWNLOAD;
            }
        } catch (IOException e) {
            Log.e("ParsePodcastTask", Log.getStackTraceString(e));
            return ERROR_DOWNLOAD;
        }

        Podcast podcast;
        try {
            PodcastParser podParser = new PodcastParser();
            podcast = podParser.parse(xml);
            podcast.setUrl(this.url);
        } catch (XmlPullParserException | IOException e) {
            Log.e("ParsePodcastTask", Log.getStackTraceString(e));
            return ERROR_PARSE;
        }

        resultObject = podcast;
        return SUCCESSFUL;
    }

    private String download(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader in;

        // Handle response code
        int response = conn.getResponseCode();
        // If response was OK, set the input stream
        if (response == 200) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            // Redirect codes
        } else if (response == 301 || response == 302) {
            return download(conn.getHeaderField("Location"));
        } else {
            return null;
        }

        // Read each byte until no more data in stream
        StringBuilder buffer = new StringBuilder();
        int data;
        while ((data = in.read()) != -1) {
            buffer.append((char) data);
        }

        in.close();
        conn.disconnect();
        return buffer.toString();
    }
}
