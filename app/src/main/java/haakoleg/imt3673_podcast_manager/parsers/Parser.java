package haakoleg.imt3673_podcast_manager.parsers;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import haakoleg.imt3673_podcast_manager.models.Podcast;

/**
 * Abstract class which all parsers for different feed formats must extend.
 * Contains common methods used by all parsers, such as reading a text node
 * and skipping tags.
 */
public abstract class Parser {
    protected final XmlPullParser parser;

    protected Parser(XmlPullParser parser) {
        this.parser = parser;
    }

    // Must be overridden, this will parse the podcast
    public abstract void parse (Podcast podcast) throws XmlPullParserException, IOException;

    protected long readDate(XmlPullParser parser, SimpleDateFormat sdf) throws IOException, XmlPullParserException {
        String toFormat = readText(parser);
        try {
            Date date = sdf.parse(toFormat);
            return date.getTime();
        } catch (ParseException e) {
            Log.e("Parser", "Unable to parse date: " + Log.getStackTraceString(e));
            return -1;
        }
    }

    protected String readAttributeValue(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        String result = parser.getAttributeValue(null, name);
        if (result != null) {
            parser.nextTag();
            return result;
        } else {
            parser.nextTag();
            return "";
        }
    }

    /**
     * Extracts the text value from a node containing a string
     * Ref: https://developer.android.com/training/basics/network-ops/xml.html
     * @param parser The xmlpullparser object
     * @return Returns the text contained in the current node, if there is no text an empty string is returned
     */
    protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Skip tags that we are not interested in
     * Ref: https://developer.android.com/training/basics/network-ops/xml.html
     * @param parser The xmlpullparser object
     */
    protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
