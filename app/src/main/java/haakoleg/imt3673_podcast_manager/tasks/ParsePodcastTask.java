package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.parsers.PodcastParser;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;

public class ParsePodcastTask extends Task<Podcast> {
    // Itunes API used for getting the feed url from an itunes link to a podcast
    private static final String ITUNES_API = "https://itunes.apple.com/lookup?id=";

    private final Context context;
    private String url;

    public ParsePodcastTask(Context context, String url, OnSuccessListener<Podcast> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.url = url.toLowerCase();
    }

    @Override
    protected int doTask() {
        if (!CheckNetwork.hasNetwork(context)) {
            return ERROR_NO_INTERNET;
        }

        // If the link is an itunes link, get the podcast URL from API
        try {
            if (this.url.matches("^http[s]?://itunes.apple.com/.*")) {
                this.url = parseItunesLink();
            }
        } catch (IOException | JSONException e) {
            Log.e("ParsePodcastTask", Log.getStackTraceString(e));
            return ERROR_PARSE;
        }

        String xml;
        try {
            xml = download();
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
            // If a "feed url" was not found while parsing, set the url to the one supplied here
            if (podcast.getUrl() == null) {
                podcast.setUrl(this.url);
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e("ParsePodcastTask", Log.getStackTraceString(e));
            return ERROR_PARSE;
        }

        resultObject = podcast;
        return SUCCESSFUL;
    }

    private String download() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Handle response code
        int response = conn.getResponseCode();
        // If response was OK, set the input stream
        BufferedReader in;
        if (response == 200) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            // Redirect codes
        } else if (response == 301 || response == 302) {
            this.url = conn.getHeaderField("Location");
            return download();
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

    /**
     * This contacts the itunes API to get the real link for the feed from an
     * itunes link. Used when the user supplies an itunes link and not feed link
     * @return The feed url retrieved from the API
     */
    private String parseItunesLink() throws IOException, JSONException {
        // Get the itunes podcast ID from the link
        Pattern pattern = Pattern.compile(".*/id([0-9]+).*");
        Matcher matcher = pattern.matcher(this.url);
        matcher.matches();
        String id = matcher.group(1);

        // Open connection to API
        URL url = new URL(ITUNES_API + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        // Read each byte until no more data in stream
        StringBuilder buffer = new StringBuilder();
        int data;
        while ((data = in.read()) != -1) {
            buffer.append((char) data);
        }
        in.close();
        conn.disconnect();

        JSONArray res = new JSONObject(buffer.toString()).getJSONArray("results");
        JSONObject resObj = res.getJSONObject(0);
        return resObj.getString("feedUrl");
    }
}
