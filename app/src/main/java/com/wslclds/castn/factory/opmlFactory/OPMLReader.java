package com.wslclds.castn.factory.opmlFactory;

import com.wslclds.castn.factory.objects.OPMLElement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class OPMLReader {

    // ATTRIBUTES
    private boolean isOPML = false;
    private ArrayList<OPMLElement> elementList;

    public ArrayList<OPMLElement> readDocument(Reader reader) throws XmlPullParserException, IOException {
        elementList = new ArrayList();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(reader);
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (xpp.getName().equals(OPMLSymbols.OPML)) {
                        isOPML = true;
                    }else if (isOPML && xpp.getName().equals(OPMLSymbols.OUTLINE)) {
                        OPMLElement element = new OPMLElement();

                        final String title = xpp.getAttributeValue(null, OPMLSymbols.TITLE);
                        if (title != null) {
                            element.setText(title);
                        } else {
                            element.setText(xpp.getAttributeValue(null, OPMLSymbols.TEXT));
                        }
                        element.setXmlUrl(xpp.getAttributeValue(null, OPMLSymbols.XMLURL));
                        element.setHtmlUrl(xpp.getAttributeValue(null, OPMLSymbols.HTMLURL));
                        element.setType(xpp.getAttributeValue(null, OPMLSymbols.TYPE));
                        if (element.getXmlUrl() != null) {
                            if (element.getText() == null) {
                                element.setText(element.getXmlUrl());
                            }
                            elementList.add(element);
                        }
                    }
                    break;
            }
            eventType = xpp.next();
        }

        return elementList;
    }
}
