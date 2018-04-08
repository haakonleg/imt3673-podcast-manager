package haakoleg.imt3673_podcast_manager.parsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import haakoleg.imt3673_podcast_manager.models.Podcast;

public class PodcastParser {
    private XmlPullParser parser;

    public PodcastParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        parser = factory.newPullParser();
    }

    public Podcast parse(String xml) throws XmlPullParserException, IOException {
        StringReader in = new StringReader(xml);
        parser.setInput(in);
        Podcast podcast = new Podcast();

        // Detect feed type and choose parser object
        Parser feedParser;
        parser.nextTag();
        if (parser.getName().equalsIgnoreCase("rss")) {
            feedParser = new RSSParser(parser);
        } else if (parser.getName().equalsIgnoreCase("feed")) {
            // TODO: Create ATOM parser
            throw new XmlPullParserException("Invalid feed type");
        } else {
            throw new XmlPullParserException("Invalid feed type");
        }

        // Parse the feed
        feedParser.parse(podcast);

        in.close();
        return podcast;
    }
}
