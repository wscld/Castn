package com.wslclds.castn.factory.opmlFactory;

import android.util.Xml;

import com.wslclds.castn.factory.objects.Podcast;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class OPMLWriter {
    private static final String ENCODING = "UTF-8";
    private static final String OPML_VERSION = "2.0";
    private static final String OPML_TITLE = "Castn";


    public void writeDocument(List<Podcast> podcasts, Writer writer) throws IllegalArgumentException, IllegalStateException, IOException {
        XmlSerializer xs = Xml.newSerializer();
        xs.setOutput(writer);

        xs.startDocument(ENCODING, false);
        xs.startTag(null, OPMLSymbols.OPML);
        xs.attribute(null, OPMLSymbols.VERSION, OPML_VERSION);

        xs.startTag(null, OPMLSymbols.HEAD);
        xs.startTag(null, OPMLSymbols.TITLE);
        xs.text(OPML_TITLE);
        xs.endTag(null, OPMLSymbols.TITLE);
        xs.endTag(null, OPMLSymbols.HEAD);

        xs.startTag(null, OPMLSymbols.BODY);
        for (Podcast podcast : podcasts) {
            xs.startTag(null, OPMLSymbols.OUTLINE);
            xs.attribute(null, OPMLSymbols.TEXT, podcast.getTitle());
            xs.attribute(null, OPMLSymbols.TITLE, podcast.getTitle());
            xs.attribute(null, OPMLSymbols.XMLURL, podcast.getUrl());
            if (podcast.getWebsite() != null) {
                xs.attribute(null, OPMLSymbols.HTMLURL, podcast.getWebsite());
            }
            xs.endTag(null, OPMLSymbols.OUTLINE);
        }
        xs.endTag(null, OPMLSymbols.BODY);
        xs.endTag(null, OPMLSymbols.OPML);
        xs.endDocument();
    }
}
