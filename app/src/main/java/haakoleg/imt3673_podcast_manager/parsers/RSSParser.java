package haakoleg.imt3673_podcast_manager.parsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

/**
 * Parses an RSS podcast feed
 */

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

            String tagName = parser.getName().toLowerCase();
            switch (tagName) {
                case "title":
                    podcast.setTitle(readText(parser));
                    break;
                case "link":
                    podcast.setLink(readText(parser));
                    break;
                case "description":
                    podcast.setDescription(readText(parser));
                    break;
                case "itunes:summary":
                    // itunes:summary contains shorter description than description, so only
                    // set if description is not already set
                    if (podcast.getDescription() == null) {
                        podcast.setDescription(readText(parser));
                    } else {
                        parser.next();
                        parser.nextTag();
                    }
                    break;
                case "itunes:category":
                    podcast.setCategory(readAttributeValue(parser, "text"));
                    break;
                case "category":
                    // Prefer itunes:category
                    if (podcast.getCategory() == null) {
                        podcast.setCategory(readText(parser));
                    } else {
                        parser.next();
                        parser.nextTag();
                    }
                    break;
                case "image":
                    podcast.setImage(readImage(parser));
                    break;
                case "itunes:image":
                    podcast.setImage(readAttributeValue(parser, "href"));
                    break;
                case "pubdate":
                    podcast.setUpdated(readDate(parser, sdf));
                    break;
                case "item":
                    podcast.addEpisode(readEpisode(parser));
                    break;
                case "itunes:new-feed-url":
                    podcast.setUrl(readText(parser));
                    break;
                default:
                    skip(parser);
            }
        }

    }

    /**
     * Parses a podcast episode, "item" tag in XML
     * @param parser The xmlpullparser object
     * @return The parsed podcast episode, as PodcastEpisode object
     */
    private PodcastEpisode readEpisode(XmlPullParser parser) throws XmlPullParserException, IOException {
        PodcastEpisode episode = new PodcastEpisode();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName().toLowerCase();
            switch (tagName) {
                case "title":
                    episode.setTitle(readText(parser));
                    break;
                case "link":
                    episode.setLink(readText(parser));
                    break;
                case "description":
                    episode.setDescription(readText(parser));
                    break;
                case "itunes:summary":
                    // itunes:summary contains shorter description than description, so only
                    // set if description is not already set
                    if (episode.getDescription() == null) {
                        episode.setDescription(readText(parser));
                    } else {
                        parser.next();
                        parser.nextTag();
                    }
                    break;
                case "enclosure":
                    // The "enclosure" tag contains media items, in this case we want to set
                    // the audio URL if the type is audio
                    String type = parser.getAttributeValue(null, "type").toLowerCase();
                    if (type.startsWith("audio")) {
                        episode.setAudioUrl(parser.getAttributeValue(null, "url"));
                    }
                    parser.nextTag();
                    break;
                case "itunes:duration":
                    episode.setDuration(readDuration(parser));
                    break;
                case "pubdate":
                    episode.setUpdated(readDate(parser, sdf));
                    break;
                default:
                    skip(parser);
            }
        }
        return episode;
    }

    /**
     * Parses the itunes:duration tag which has the format HH:mm:ss or mm:ss or ss
     * @return Total duration in seconds
     */
    private int readDuration(XmlPullParser parser) throws XmlPullParserException, IOException {
        String duration = readText(parser);

        int seconds = 0;
        String[] vals = duration.split(":");
        for (int i = 0; i < vals.length; i++) {
            int value = Integer.parseInt(vals[i]);
            seconds += (value * Math.pow(60, (double)(vals.length-1) - i));
        }
        return seconds;
    }

    /**
     * Reads an "image" tag in the RSS format
     * @param parser The xmlpullparser object
     * @return Link to the image
     */
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
