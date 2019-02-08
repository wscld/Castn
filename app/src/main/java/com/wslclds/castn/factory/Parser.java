package com.wslclds.castn.factory;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.helpers.Helper;

public class Parser {
    private Context context;

    public Parser(Context context){
        this.context = context;
    }

    public Podcast parsePodcast(String url){
        Podcast podcast = new Podcast();
        Document doc = Jsoup.parse(new DataGetter(context).getFeed(url));
        Element element = doc.select("channel").first();


        if(element != null){
            ElementsGetter elementsGetter = new ElementsGetter(element);
            podcast.setId(Helper.urlToId(url));
            podcast.setAuthor(elementsGetter.getPodcastAuthor());
            podcast.setCategory(elementsGetter.getPodcastCategory());
            podcast.setDescription(elementsGetter.getPodcastDescription());
            podcast.setPlainDescription(elementsGetter.getPodcastPlainDescription());
            podcast.setWebsite(elementsGetter.getPodcastWebsite());
            podcast.setImage(elementsGetter.getPodcastImage());
            podcast.setLanguage(elementsGetter.getPodcastLanguage());
            podcast.setUrl(url);
            podcast.setTitle(elementsGetter.getPodcastTitle());
            podcast.setPubDate(elementsGetter.getPodcastPubDate());
            podcast.setCopyright(elementsGetter.getPodcastCopyright());
            podcast.setExplicit(elementsGetter.isPodcastExplicit());
        }

        return podcast;
    }


    public ArrayList<Episode> parseEpisodes(String url, boolean fastParse){
        int count = 0;
        ArrayList<Episode> episodes = new ArrayList<>();

        Document doc = Jsoup.parse(new DataGetter(context).getFeed(url));
        Element podcastChanel = doc.select("channel").first();
        final Elements items = doc.select("item");
        ElementsGetter podcastElementsGetter = new ElementsGetter(podcastChanel);


        for (Element item : items) {
            if(fastParse && count >= 50){
                break;
            }
            ElementsGetter elementsGetter = new ElementsGetter(item);
            Episode episode = new Episode();
            episode.setId(Helper.urlToId(elementsGetter.getEpisodeEnclosureUrl()));
            episode.setTitle(elementsGetter.getEpisodeTitle());
            episode.setPodcastTitle(podcastElementsGetter.getPodcastTitle());
            episode.setImage(elementsGetter.getEpisodeImage());
            episode.setPubDate(elementsGetter.getEpisodePubDate());
            episode.setDescription(elementsGetter.getEpisodeDescription());
            episode.setPlainDescription(elementsGetter.getEpisodePlainDescription());
            episode.setAuthor(elementsGetter.getEpisodeAuthor());
            episode.setPodcastAuthor(elementsGetter.getPodcastAuthor());
            episode.setCategory(elementsGetter.getEpisodeCategory());
            episode.setDuration(elementsGetter.getEpisodeDuration());
            episode.setEnclosureLength(elementsGetter.getEpisodeEnclosureLength());
            episode.setEnclosureType(elementsGetter.getEpisodeEnclosureType());
            episode.setEnclosureUrl(elementsGetter.getEpisodeEnclosureUrl());
            episode.setUrl(url);
            episode.setExplicit(elementsGetter.isEpisodeExplicit());
            if (episode.getImage().equals("")) {
                episode.setImage(podcastElementsGetter.getPodcastImage());
            }
            episodes.add(episode);
            count++;
        }
        return episodes;
    }

    public ArrayList<Episode> parseEpisodesUntil(String url, long date, long cacheTime){
        ArrayList<Episode> episodes = new ArrayList<>();

        if(url != null && url.length() > 0) {
            Document doc = Jsoup.parse(new DataGetter(context).getFeed(url, cacheTime));
            Element podcastChanel = doc.select("channel").first();
            final Elements items = doc.select("item");
            ElementsGetter podcastElementsGetter = new ElementsGetter(podcastChanel);

            for (Element item : items) {
                ElementsGetter elementsGetter = new ElementsGetter(item);
                Episode episode = new Episode();
                episode.setId(Helper.urlToId(elementsGetter.getEpisodeEnclosureUrl()));
                episode.setTitle(elementsGetter.getEpisodeTitle());
                episode.setPodcastTitle(podcastElementsGetter.getPodcastTitle());
                episode.setImage(elementsGetter.getEpisodeImage());
                episode.setPubDate(elementsGetter.getEpisodePubDate());
                episode.setDescription(elementsGetter.getEpisodeDescription());
                episode.setPlainDescription(elementsGetter.getEpisodePlainDescription());
                episode.setAuthor(elementsGetter.getEpisodeAuthor());
                episode.setPodcastAuthor(elementsGetter.getPodcastAuthor());
                episode.setCategory(elementsGetter.getEpisodeCategory());
                episode.setDuration(elementsGetter.getEpisodeDuration());
                episode.setEnclosureLength(elementsGetter.getEpisodeEnclosureLength());
                episode.setEnclosureType(elementsGetter.getEpisodeEnclosureType());
                episode.setEnclosureUrl(elementsGetter.getEpisodeEnclosureUrl());
                episode.setUrl(url);
                episode.setExplicit(elementsGetter.isEpisodeExplicit());
                if (episode.getImage().equals("")) {
                    episode.setImage(podcastElementsGetter.getPodcastImage());
                }

                if (episode.getPubDate() <= date) {
                    break;
                } else {
                    episodes.add(episode);
                }
            }
        }
        return episodes;
    }
}
