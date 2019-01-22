package com.wslclds.castn.factory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ElementsGetter {

    private Element element;


    public ElementsGetter(Element element){
        this.element = element;
    }


    public String getPodcastTitle(){
        String str = "";
        if(element.select("title").first() != null) {
            str = element.select("title").first().text();
        }
        return Jsoup.clean(str,Whitelist.none()).replace("&amp;","&");
    }

    public String getPodcastWebsite(){
        String str = "";
        if(element.select("link").first() != null) {
            str = element.select("link").first().text();
        }
        return str;
    }

    public String getPodcastAuthor(){
        String str = "";
        if(element.select("itunes|author").first() != null) {
            str = element.select("itunes|author").first().text();
        }
        return str;
    }

    public boolean isPodcastExplicit(){
        boolean explicit = false;
        if(element.select("itunes|explicit").first() != null) {
            if(element.select("itunes|explicit").first().text().equals("yes")){
                explicit = true;
            }
        }
        return explicit;
    }

    public String getPodcastImage(){
        String str = "";
        if(element.select("itunes|image").attr("href") != null) {
            str = element.select("itunes|image").attr("href");
        }
        return str;
    }

    public String getPodcastDescription(){
        String str = "";
        if(element.select("description").first() != null) {
            str = element.select("description").first().text();
        }
        return str;
    }

    public String getPodcastPlainDescription(){
        String str = "";
        if(element.select("description").first() != null) {
            str = Jsoup.parse(element.select("description").first().text()).text();
        }
        return str;
    }

    public String getPodcastLanguage(){
        String str = "";
        if(element.select("language").first() != null) {
            str = element.select("language").first().text();
        }
        return str;
    }

    public long getPodcastPubDate(){
        long date = 0;
        if(element.select("pubDate").first() != null) {
            if (stringToDate(element.select("pubDate").first().text()) != null) {
                date = stringToDate(element.select("pubDate").first().text()).getTime();
            } else {
                date = 0;
            }
        }
        return date;
    }

    public String getPodcastCopyright(){
        String str = "";
        if(element.select("copyright").first() != null) {
            str = element.select("copyright").first().text();
        }
        return str;
    }

    public String getPodcastCategory(){
        String str = "";
        if(element.select("itunes|category").first() != null) {
            for(Element el : element.select("itunes|category")){
                if(el.attr("text") != null){
                    if(str.equals("")){
                        str += el.attr("text");
                    }else {
                        str += ","+el.attr("text");
                    }
                }
            }
        }
        return str;
    }


    /*

        EPISODE GETTERS BELOW

     */

    public String getEpisodeTitle(){
        String str = "";
        if (element.select("title").first() != null) {
            str = element.select("title").first().text();
        }
        return Jsoup.clean(str,Whitelist.none()).replace("&amp;","&");
    }

    public String getEpisodeAuthor(){
        String str = "";
        if (element.select("author").first() != null) {
            str = element.select("author").first().text();
        }
        return str;
    }

    public String getEpisodeCategory(){
        String str = "";
        if (element.select("category").first() != null) {
            str = element.select("category").first().text();
        }
        return str;
    }

    public long getEpisodePubDate(){
        long date = 0;
        if(element.select("pubDate").first() != null) {
            if(stringToDate(element.select("pubDate").first().text()) != null) {
                date = stringToDate(element.select("pubDate").first().text()).getTime();
            }else {
                date = 0;
            }
        }
        return date;
    }

    public String getEpisodeDuration(){
        String str = "";
        if (element.select("itunes|duration").first() != null) {
            str = element.select("itunes|duration").first().text();
        }
        if(str.equals("")){
            if (element.select("duration").first() != null) {
                str = element.select("duration").first().text();
            }
        }
        return str;
    }

    public boolean isEpisodeExplicit(){
        boolean explicit = false;
        if(element.select("itunes|explicit").first() != null) {
            if(element.select("itunes|explicit").first().text().equals("yes")){
                explicit = true;
            }
        }
        return explicit;
    }

    public String getEpisodeImage(){
        String str = "";
        if (element.select("itunes|image").attr("href") != null) {
            str = element.select("itunes|image").attr("href");
        }
        return str;
    }

    public String getEpisodeDescription(){
        String str = "";
        if (element.select("description").first() != null) {
            str = element.select("description").first().text();
        }
        return str;
    }

    public String getEpisodePlainDescription(){
        String str = "";
        if (element.select("description").first() != null) {
            str = Jsoup.parse(element.select("description").first().text()).text();
        }
        return str;
    }

    public String getEpisodeEnclosureUrl(){
        String str = "";
        if (element.select("enclosure") != null && element.select("enclosure").attr("url") != null) {
            str = element.select("enclosure").attr("url");
        }
        return str;
    }

    public long getEpisodeEnclosureLength(){
        long length = 0;
        if (element.select("enclosure") != null && element.select("enclosure").attr("length") != null && element.select("enclosure").attr("length").toString().length() > 0) {
            try{
                length = Long.parseLong(element.select("enclosure").attr("length").replaceAll("[^0-9]", ""));
            }catch (Exception e){}
        }
        return length;
    }

    public String getEpisodeEnclosureType(){
        String str = "";
        if (element.select("enclosure") != null && element.select("enclosure").attr("type") != null) {
            str = element.select("enclosure").attr("type");
        }
        return str;
    }


    private Date stringToDate(String date){
        if(date != null) {
            DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            try {
                return formatter.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}