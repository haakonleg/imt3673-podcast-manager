package haakoleg.imt3673_podcast_manager.parsers;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

public class RSSParser extends Parser {
    private final SimpleDateFormat sdf;

    public RSSParser(XmlPullParser parser) {
        super(parser);
        // RFC 822, the time format used in RSS 2.0
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    }

    @Override
    public void parse(Podcast podcast) throws XmlPullParserException, IOException {
        // RSS feed must start with "channel" tag
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "channel");

        // Read tags until encounter end tag
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            if (tagName.equalsIgnoreCase("title")) {
                podcast.setTitle(readText(parser));
            } else if (tagName.equalsIgnoreCase("link")) {
                podcast.setLink(readText(parser));
            } else if (tagName.equalsIgnoreCase("description")) {
                podcast.setDescription(readText(parser));
            } else if (tagName.equalsIgnoreCase("itunes:summary")) {
                // itunes:summary contains shorter description than description, so only
                // set if description is not already set
                if (podcast.getDescription() == null) {
                    podcast.setDescription(readText(parser));
                } else {
                    parser.next();
                    parser.nextTag();
                }
            } else if (tagName.equalsIgnoreCase("itunes:category")) {
                podcast.setCategory(readAttributeValue(parser, "text"));
            } else if (tagName.equalsIgnoreCase("category")) {
                // Prefer itunes:category
                if (podcast.getCategory() == null) {
                    podcast.setCategory(readText(parser));
                } else {
                    parser.next();
                    parser.nextTag();
                }
            } else if (tagName.equalsIgnoreCase("image")) {
                podcast.setImage(readImage(parser));
            } else if (tagName.equalsIgnoreCase("itunes:image")) {
                podcast.setImage(readAttributeValue(parser, "href"));
            } else if (tagName.equalsIgnoreCase("pubDate")) {
                podcast.setUpdated(readDate(parser, sdf));
            } else if (tagName.equalsIgnoreCase("item")) {
                podcast.addEpisode(readEpisode(parser));
            } else {
                skip(parser);
            }
        }

    }

    private PodcastEpisode readEpisode(XmlPullParser parser) throws XmlPullParserException, IOException {
        PodcastEpisode episode = new PodcastEpisode();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            if (tagName.equalsIgnoreCase("title")) {
                episode.setTitle(readText(parser));
            } else if (tagName.equalsIgnoreCase("link")) {
                episode.setLink(readText(parser));
            } else if (tagName.equalsIgnoreCase("description")) {
                episode.setDescription(readText(parser));
            } else if (tagName.equalsIgnoreCase("itunes:summary")) {
                // itunes:summary contains shorter description than description, so only
                // set if description is not already set
                if (episode.getDescription() == null) {
                    episode.setDescription(readText(parser));
                } else {
                    parser.next();
                    parser.nextTag();
                }
            } else if (tagName.equalsIgnoreCase("enclosure")) {
                // The "enclosure" tag contains media items, in this case we want to set
                // the audio URL if the type is audio
                String type = parser.getAttributeValue(null, "type").toLowerCase();
                if (type.startsWith("audio")) {
                    episode.setAudioUrl(parser.getAttributeValue(null, "url"));
                }
                parser.nextTag();
            } else if (tagName.equalsIgnoreCase("itunes:duration")) {
                episode.setDuration(readDuration(parser));
            } else if (tagName.equalsIgnoreCase("pubDate")) {
                episode.setUpdated(readDate(parser, sdf));
            } else {
                skip(parser);
            }
        }

        return episode;
    }

    private int readDuration(XmlPullParser parser) throws XmlPullParserException, IOException {
        String duration = readText(parser);
        Log.e("DURATION", duration);

        int seconds = 0;
        String[] vals = duration.split(":");
        for (int i = 0; i < vals.length; i++) {
            int value = Integer.parseInt(vals[i]);
            seconds += (value * Math.pow(60, (vals.length-1) - i));
        }
        return seconds;
    }

    private String readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";

        while(parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (parser.getName().equalsIgnoreCase("url")) {
                result = readText(parser);
            } else {
                skip(parser);
            }
        }

        return result;
    }
}
