package com.wslclds.castn.factory;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.wslclds.castn.factory.objects.DBToken;
import com.wslclds.castn.factory.objects.DeviceId;
import com.wslclds.castn.factory.objects.Download;
import com.wslclds.castn.factory.objects.Episode;
import com.wslclds.castn.factory.objects.Feed;
import com.wslclds.castn.factory.objects.Pagination;
import com.wslclds.castn.factory.objects.Playlist;
import com.wslclds.castn.factory.objects.PlaylistEpisode;
import com.wslclds.castn.factory.objects.Podcast;
import com.wslclds.castn.factory.objects.PodcastAndEpisode;
import com.wslclds.castn.factory.objects.Time;
import com.wslclds.castn.factory.objects.PodcastUpload;
import com.wslclds.castn.helpers.Helper;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class DatabaseManager {
    private Context context;

    private RealmConfiguration realmConfig() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        return config;
    }

    public DatabaseManager(Context context){
        this.context = context;
    }

    public Realm getRealmInstance(){
        return Realm.getInstance(realmConfig());
    }

    //manager starts here

    public void registerDeviceId(){
        Realm realm = getRealmInstance();
        DeviceId deviceId = realm.where(DeviceId.class).findFirst();
        if(deviceId == null){
            realm.beginTransaction();
            deviceId = realm.createObject(DeviceId.class);
            deviceId.setDeviceId(UUID.randomUUID().toString());
            realm.commitTransaction();
        }
        realm.close();
    }

    public DeviceId getDeviceId(){
        Realm realm = getRealmInstance();
        DeviceId deviceId = realm.where(DeviceId.class).findFirst();
        if(deviceId != null){
            deviceId = realm.copyFromRealm(deviceId);
        }else {
            registerDeviceId();
            deviceId = getDeviceId();
        }
        realm.close();
        return deviceId;
    }

    public void cacheFeed(String url, String feed){
        Realm realm = getRealmInstance();

        Feed feedObject = realm.where(Feed.class).contains("url",url).findFirst();
        if(feedObject == null){
            realm.beginTransaction();
            feedObject = realm.createObject(Feed.class,UUID.randomUUID().toString());
            feedObject.setFeed(feed);
            feedObject.setUrl(url);
            feedObject.setDate(new Date().getTime());
            realm.commitTransaction();
        }else{
            realm.beginTransaction();
            feedObject.setFeed(feed);
            feedObject.setUrl(url);
            feedObject.setDate(new Date().getTime());
            realm.commitTransaction();
        }
        realm.close();
    }

    public Feed getCachedFeed(String url){
        Realm realm = getRealmInstance();

        Feed feed = realm.where(Feed.class).contains("url",url).findFirst();
        if(feed != null){
            feed = realm.copyFromRealm(feed);
            realm.close();
            return feed;
        }
        realm.close();
        return null;
    }

    public boolean isSubscribed(String url){
        Realm realm = getRealmInstance();

        Podcast podcast = realm.where(Podcast.class).contains("url",url).findFirst();
        if(podcast != null){
            realm.close();
            return true;
        }else {
            realm.close();
            return false;
        }
    }

    public boolean isSubscribedId(String id){
        Realm realm = getRealmInstance();

        Podcast podcast = realm.where(Podcast.class).contains("id",id).findFirst();
        if(podcast != null){
            realm.close();
            return true;
        }else {
            realm.close();
            return false;
        }
    }

    public void subscribe(String url,Podcast podcast, ArrayList<Episode> episodes, DatabaseUpdateListener databaseUpdateListener){
        if(!isSubscribed(url)){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(podcast);
                    realm.copyToRealmOrUpdate(episodes);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public void unsubscribe(String url, DatabaseUpdateListener databaseUpdateListener){
        if(isSubscribed(url)){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url", url).findAll();
                    episodesRealm.deleteAllFromRealm();
                    Podcast podcastRealm = realm.where(Podcast.class).contains("url", url).findFirst();
                    if (podcastRealm != null) {
                        podcastRealm.deleteFromRealm();
                    }

                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public void storePodcast(Podcast podcast){
        Realm realm = getRealmInstance();

        Podcast podcastRealm = realm.where(Podcast.class).contains("url",podcast.getUrl()).findFirst();
        if(podcastRealm == null){
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(podcast);
            realm.commitTransaction();
        }
        realm.close();
    }

    public Podcast getPodcast(String url){
        Realm realm = getRealmInstance();

        Podcast podcastRealm = realm.where(Podcast.class).contains("url",url).findFirst();
        if(podcastRealm != null){
            podcastRealm = realm.copyFromRealm(podcastRealm);
            realm.close();
            return podcastRealm;
        }
        realm.close();
        return null;
    }

    public Podcast getPodcastWithId(String id){
        Realm realm = getRealmInstance();

        Podcast podcastRealm = realm.where(Podcast.class).contains("id",id).findFirst();
        if(podcastRealm != null){
            podcastRealm = realm.copyFromRealm(podcastRealm);
            realm.close();
            return podcastRealm;
        }
        realm.close();
        return null;
    }

    public void storePodcasts(ArrayList<Podcast> podcasts, DatabaseUpdateListener databaseUpdateListener){
        if(podcasts.size() > 0 && podcasts.get(0) != null){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(podcasts);
                    realm.commitTransaction();
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public void storePlaylists(ArrayList<Playlist> playlists, DatabaseUpdateListener databaseUpdateListener){
        if(playlists.size() > 0 && playlists.get(0) != null){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(playlists);
                    realm.commitTransaction();
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public void storePlaylistEpisodes(ArrayList<PlaylistEpisode> playlistEpisodes, DatabaseUpdateListener databaseUpdateListener){
        if(playlistEpisodes.size() > 0 && playlistEpisodes.get(0) != null){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(playlistEpisodes);
                    realm.commitTransaction();
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public Podcast getLatestPodcast(){
        Realm realm = getRealmInstance();

        Podcast podcastRealm = realm.where(Podcast.class).sort("pubDate",Sort.DESCENDING).findFirst();
        if(podcastRealm != null){
            podcastRealm = realm.copyFromRealm(podcastRealm);
            realm.close();
            return podcastRealm;
        }
        realm.close();
        return null;
    }

    public int getColorForPodcast(String url){
        Realm realm = getRealmInstance();
        int color = 0;

        Podcast podcastRealm = realm.where(Podcast.class).contains("url",url).findFirst();
        if(podcastRealm != null){
            color = podcastRealm.getColor();
        }
        realm.close();
        return color;
    }

    public List<Podcast> getPodcasts(){
        Realm realm = getRealmInstance();
        realm.refresh();
        RealmResults<Podcast> podcastsRealm = realm.where(Podcast.class).findAll().sort("pubDate",Sort.DESCENDING);
        return realm.copyFromRealm(podcastsRealm);
    }

    public void updatePodcastPubDate(String url){
        Realm realm = getRealmInstance();
        realm.refresh();

        Podcast podcastRealm = realm.where(Podcast.class).contains("url",url).findFirst();
        Episode latestEpisode = getLatestStoredEpisode(url);
        if(latestEpisode != null && latestEpisode.isValid()){
            realm.beginTransaction();
            podcastRealm.setPubDate(latestEpisode.getPubDate());
            realm.commitTransaction();
        }
        realm.close();
    }

    public ArrayList<Podcast> searchPodcasts(String query){
        Realm realm = getRealmInstance();

        RealmResults<Podcast> podcastsRealm = realm.where(Podcast.class).contains("title",query, Case.INSENSITIVE).or().contains("description",query, Case.INSENSITIVE).findAll().sort("pubDate",Sort.DESCENDING);
        ArrayList<Podcast> podcasts = new ArrayList<>();
        for(Podcast podcast : podcastsRealm){
            if(podcast != null){
                podcasts.add(realm.copyFromRealm(podcast));
            }
        }
        realm.close();
        return podcasts;
    }


    public void storeNewEpisodes(String url, ArrayList<Episode> episodes){

        Episode latestEpisode = getLatestStoredEpisode(url);

        if(latestEpisode != null && episodes.size() > 0 && episodes.get(0) != null){
            for(Episode episode : episodes){
                if(episode.getPubDate() == latestEpisode.getPubDate()) {
                    break;
                }else {
                    storeEpisode(episode);
                }
            }
        }
    }

    public void storeEpisode(Episode episode){
        Realm realm = getRealmInstance();

        Episode episodeRealm = realm.where(Episode.class).contains("enclosureUrl",episode.getEnclosureUrl()).findFirst();
        if(episodeRealm == null){
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(episode);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void storeEpisodes(ArrayList<Episode> episodes){
        if(episodes.size() > 0 && episodes.get(0) != null){
            for(Episode episode : episodes){
                storeEpisode(episode);
            }
        }
    }

    public void storeEpisodes(ArrayList<Episode> episodes, DatabaseUpdateListener databaseUpdateListener){
        if(episodes.size() > 0 && episodes.get(0) != null){
            Realm realm = getRealmInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(episodes);
                    realm.commitTransaction();
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    databaseUpdateListener.onSuccess();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    databaseUpdateListener.onFailure();
                }
            });
        }
    }

    public Episode getEpisode(String enclosureUrl){
        Realm realm = getRealmInstance();

        Episode episodeRealm = realm.where(Episode.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(episodeRealm != null){
            episodeRealm = realm.copyFromRealm(episodeRealm);
            realm.close();
            return episodeRealm;
        }
        realm.close();
        return null;
    }

    public Episode getLatestStoredEpisode(String url){
        Realm realm = getRealmInstance();

        Episode episodeRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.DESCENDING).findFirst();
        if(episodeRealm != null){
            episodeRealm = realm.copyFromRealm(episodeRealm);
            realm.close();
            return episodeRealm;
        }
        realm.close();
        return null;
    }


    public long getLatestPubDate(String url){
        Realm realm = getRealmInstance();
        long latestPubDate = 0;

        Episode episodeRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.DESCENDING).findFirst();
        if(episodeRealm != null){
            if(episodeRealm != null){
                latestPubDate = episodeRealm.getPubDate();
            }else {
                Podcast podcastRealm = realm.where(Podcast.class).contains("url",url).findFirst();
                latestPubDate = podcastRealm.getPubDate();
            }
        }
        realm.close();
        return latestPubDate;
    }


    public ArrayList<Episode> getEpisodes(String url, Pagination pagination){
        Realm realm = getRealmInstance();

        int count = 0;
        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episodes.size() <= pagination.getLimit() && count > pagination.getBegin()){
                    if(episode != null){
                        episodes.add(realm.copyFromRealm(episode));
                    }
                }else {
                    break;
                }
                count++;
            }
        }
        realm.close();
        return episodes;
    }

    public ArrayList<Episode> getEpisodes(Pagination pagination){
        Realm realm = getRealmInstance();

        int count = 0;
        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).findAll().sort("pubDate",Sort.DESCENDING);
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(count <= pagination.getBegin()+pagination.getLimit() && count >= pagination.getBegin()){
                    if(episode != null){
                        episodes.add(realm.copyFromRealm(episode));
                    }
                }else if(count > pagination.getBegin()+pagination.getLimit()){
                    break;
                }
                count++;
            }
        }
        realm.close();
        return episodes;
    }

    public ArrayList<Episode> getEpisodes(String url){
        Realm realm = getRealmInstance();
        realm.refresh();

        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.DESCENDING).findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    episodes.add(realm.copyFromRealm(episode));
                }
            }
        }
        realm.close();
        return episodes;
    }

    public ArrayList<Episode> getEpisodesAscending(String url){
        Realm realm = getRealmInstance();
        realm.refresh();

        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.ASCENDING).findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    episodes.add(realm.copyFromRealm(episode));
                }
            }
        }
        realm.close();
        return episodes;
    }

    public List<Episode> getEpisodes(String url, int limit){
        Realm realm = getRealmInstance();
        realm.refresh();

        List<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.DESCENDING).findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    episodes.add(realm.copyFromRealm(episode));
                }
            }
        }
        realm.close();
        if(episodes.size() > limit){
            return episodes.subList(0,limit);
        }
        return episodes;
    }

    public long getFrequency(String url){
        Realm realm = getRealmInstance();
        realm.refresh();
        int limit = 10;
        int i = 0;

        ArrayList<Long> dates = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).sort("pubDate",Sort.DESCENDING).findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    if(i < limit){
                        dates.add(episode.getPubDate());
                        i++;
                    }else {
                        break;
                    }
                }
            }
        }

        long frequency = 0;
        for(Long l : dates){
            frequency +=l;
        }
        frequency = frequency/dates.size();

        realm.close();
        return frequency;
    }

    public ArrayList<Episode> searchEpisodes(String url, String query){
        Realm realm = getRealmInstance();
        realm.refresh();

        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class)
                .contains("url",url).and()
                .beginGroup()
                .contains("description",query,Case.INSENSITIVE).or()
                .contains("title",query,Case.INSENSITIVE)
                .endGroup()
                .sort("pubDate",Sort.DESCENDING)
                .findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    episodes.add(realm.copyFromRealm(episode));
                }
            }
        }
        realm.close();
        return episodes;
    }

    public ArrayList<Episode> searchEpisodes(String query){
        Realm realm = getRealmInstance();
        realm.refresh();

        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<Episode> episodesRealm = realm.where(Episode.class)
                .beginGroup()
                .contains("description",query,Case.INSENSITIVE).or()
                .contains("title",query,Case.INSENSITIVE)
                .endGroup()
                .sort("pubDate",Sort.DESCENDING)
                .findAll();
        if(episodesRealm != null){
            for(Episode episode : episodesRealm){
                if(episode != null){
                    episodes.add(realm.copyFromRealm(episode));
                }
            }
        }
        realm.close();
        return episodes;
    }

    public ArrayList<PodcastAndEpisode> getTimeline(long lastDate, int limit){
        Realm realm = getRealmInstance();

        int count = 0;
        ArrayList<PodcastAndEpisode> podcastAndEpisodes = new ArrayList();
        List<Episode> episodes = realm.copyFromRealm(realm.where(Episode.class).lessThan("pubDate",lastDate).sort("pubDate",Sort.DESCENDING).findAll());
        if(episodes != null){
            for(Episode episode : episodes){
                if(count <= limit){
                    if(episode != null) {
                        Podcast podcastRealm = realm.where(Podcast.class).contains("url",episode.getUrl()).findFirst();
                        if (podcastRealm != null) {
                            Podcast podcast = realm.copyFromRealm(podcastRealm);
                            PodcastAndEpisode podcastAndEpisode = new PodcastAndEpisode();
                            podcastAndEpisode.setPodcast(podcast);
                            podcastAndEpisode.setEpisode(episode);
                            podcastAndEpisodes.add(podcastAndEpisode);
                        }
                    }
                }else {
                    break;
                }
                count++;
            }
        }
        realm.close();
        return podcastAndEpisodes;
    }

    public void setColor(String url, int color){
        Realm realm = getRealmInstance();

        Podcast podcast = realm.where(Podcast.class).contains("url",url).findFirst();
        if(podcast != null){
            realm.beginTransaction();
            podcast.setColor(color);
            realm.commitTransaction();
        }else {
            Episode episode = realm.where(Episode.class).contains("enclosureUrl",url).findFirst();
            if(episode != null){
                episode.setColor(color);
            }
        }
        realm.close();
    }

    public Playlist getPlaylist(String playlistId){
        Realm realm = getRealmInstance();

        Playlist playlist= realm.where(Playlist.class).contains("id",playlistId).findFirst();
        if(playlist != null){
            playlist = realm.copyFromRealm(playlist);
            System.out.println("playlist::"+playlist.getName());
            realm.close();
            return playlist;
        }
        realm.close();
        return null;
    }

    public ArrayList<Playlist> getPlaylists(){
        Realm realm = getRealmInstance();

        ArrayList<Playlist> playlists = new ArrayList<>();
        RealmResults<Playlist> playlistsRealm= realm.where(Playlist.class).findAll().sort("date",Sort.DESCENDING);
        if(playlistsRealm != null){
            for(Playlist playlist : playlistsRealm){
                playlists.add(realm.copyFromRealm(playlist));
            }
        }
        realm.close();
        return playlists;
    }

    public void createPlaylist(String playlistName, boolean publicAvailable){
        Realm realm = getRealmInstance();

        realm.beginTransaction();
        Playlist playlist = realm.createObject(Playlist.class,UUID.randomUUID().toString());
        playlist.setDate(new Date().getTime());
        playlist.setPublicAvailable(publicAvailable);
        playlist.setName(playlistName);
        realm.commitTransaction();
        realm.close();
    }

    public void createPlaylist(Playlist playlist){
        Realm realm = getRealmInstance();
        if(playlist != null) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(playlist);
            realm.commitTransaction();
            realm.close();
        }
    }

    public void editPlaylist(String playlistId, String playlistName, boolean publicAvailable){
        Realm realm = getRealmInstance();

        Playlist playlist= realm.where(Playlist.class).contains("id",playlistId).findFirst();

        if(playlist != null) {
            realm.beginTransaction();
            playlist.setDate(new Date().getTime());
            playlist.setPublicAvailable(publicAvailable);
            playlist.setName(playlistName);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void deletePlaylist(String playlistId){
        Realm realm = getRealmInstance();

        Playlist playlist = realm.where(Playlist.class).contains("id",playlistId).findFirst();
        RealmResults<PlaylistEpisode> playlistEpisodes = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).findAll();

        if(playlist != null) {
            realm.beginTransaction();
            playlist.deleteFromRealm();
            playlistEpisodes.deleteAllFromRealm();
            realm.commitTransaction();
        }
        realm.close();
    }

    public ArrayList<Episode> getPlaylistEpisodes(String playlistId){
        Realm realm = getRealmInstance();
        realm.refresh();

        ArrayList<Episode> episodes = new ArrayList();
        RealmResults<PlaylistEpisode> playlistEpisodes = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).sort("position",Sort.ASCENDING).findAll();
        if(playlistEpisodes != null){
            for(PlaylistEpisode playlistEpisode : playlistEpisodes){
                Episode episode = new Gson().fromJson(playlistEpisode.getEpisodeJson(),Episode.class);
                if(episode != null){
                    episodes.add(episode);
                }
            }
        }
        realm.close();
        return episodes;
    }

    public void cleanUpQueue(){
        Realm realm = getRealmInstance();
        RealmResults<PlaylistEpisode> playlistEpisodesRealm = realm
                .where(PlaylistEpisode.class)
                .contains("playlistId",Helper.DEFAULT_PLAYLIST_ID)
                .sort("position",Sort.ASCENDING)
                .findAll();

        if(playlistEpisodesRealm.size() > 10){
            realm.beginTransaction();
            for(int i = 10; i < playlistEpisodesRealm.size(); i++){
                playlistEpisodesRealm.get(i).deleteFromRealm();
            }
            realm.commitTransaction();
        }
        realm.close();
    }

    public ArrayList<PlaylistEpisode> getRawPlaylistEpisodes(String playlistId){
        Realm realm = getRealmInstance();
        realm.refresh();
        ArrayList<PlaylistEpisode> playlistEpisodes = new ArrayList();
        RealmResults<PlaylistEpisode> playlistEpisodesRealm = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).sort("position",Sort.ASCENDING).findAll();
        if(playlistEpisodesRealm != null){
            for(PlaylistEpisode playlistEpisode : playlistEpisodesRealm){
                if(playlistEpisode != null){
                    playlistEpisodes.add(realm.copyFromRealm(playlistEpisode));
                }
            }
        }
        realm.close();
        return playlistEpisodes;
    }

    public ArrayList<PlaylistEpisode> getRawPlaylistEpisodes(){
        Realm realm = getRealmInstance();

        ArrayList<PlaylistEpisode> playlistEpisodes = new ArrayList();
        RealmResults<PlaylistEpisode> playlistEpisodesRealm = realm.where(PlaylistEpisode.class).notEqualTo("playlistId",Helper.DEFAULT_PLAYLIST_ID).sort("position",Sort.ASCENDING).findAll();
        if(playlistEpisodesRealm != null){
            for(PlaylistEpisode playlistEpisode : playlistEpisodesRealm){
                if(playlistEpisode != null){
                    playlistEpisodes.add(realm.copyFromRealm(playlistEpisode));
                }
            }
        }
        realm.close();
        return playlistEpisodes;
    }

    public PlaylistEpisode getPlaylistEpisode(String enclosureUrl, String playlistId){
        Realm realm = getRealmInstance();

        PlaylistEpisode playlistEpisode = realm.where(PlaylistEpisode.class).contains("enclosureUrl",enclosureUrl).contains("playlistId",playlistId).findFirst();
        if(playlistEpisode != null){
            playlistEpisode = realm.copyFromRealm(playlistEpisode);
        }
        realm.close();
        return playlistEpisode;
    }

    public void addEpisodeToPlaylist(Episode episode, String playlistId){
        episode.setDescription(null);

        Realm realm = getRealmInstance();
        PlaylistEpisode playlistEpisode = realm
                .where(PlaylistEpisode.class)
                .contains("playlistId",playlistId)
                .contains("enclosureUrl",episode.getEnclosureUrl())
                .findFirst();

        if(playlistEpisode == null){
            openPositionSpace(0,playlistId);
            realm.beginTransaction();
            playlistEpisode = realm.createObject(PlaylistEpisode.class,UUID.randomUUID().toString());
            playlistEpisode.setEpisodeJson(new Gson().toJson(episode));
            playlistEpisode.setEnclosureUrl(episode.getEnclosureUrl());
            playlistEpisode.setUrl(episode.getUrl());
            playlistEpisode.setPosition(0);
            playlistEpisode.setPlaylistId(playlistId);
            realm.commitTransaction();
            cleanUpPositions(playlistId);
        }else{
            realm.beginTransaction();
            playlistEpisode.deleteFromRealm();
            realm.commitTransaction();
            cleanUpPositions(playlistId);

            openPositionSpace(0,playlistId);
            realm.beginTransaction();
            playlistEpisode = realm.createObject(PlaylistEpisode.class,UUID.randomUUID().toString());
            playlistEpisode.setEpisodeJson(new Gson().toJson(episode));
            playlistEpisode.setEnclosureUrl(episode.getEnclosureUrl());
            playlistEpisode.setUrl(episode.getUrl());
            playlistEpisode.setPosition(0);
            playlistEpisode.setPlaylistId(playlistId);
            realm.commitTransaction();
            cleanUpPositions(playlistId);
        }
    }

    public void addEpisodeToPlaylist(PlaylistEpisode playlistEpisode){
        Realm realm = getRealmInstance();
        PlaylistEpisode playlistEpisodeRealm = realm.where(PlaylistEpisode.class)
                .contains("playlistId",playlistEpisode.getPlaylistId())
                .contains("enclosureUrl",playlistEpisode.getEnclosureUrl())
                .findFirst();

        if(playlistEpisode != null && playlistEpisodeRealm == null){
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(playlistEpisode);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void clearPlaylist(String playlistId){
        Realm realm = getRealmInstance();

        RealmResults<PlaylistEpisode> playlistEpisodes = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).findAll().sort("position",Sort.ASCENDING);
        realm.beginTransaction();
        for(PlaylistEpisode playlistEpisode : playlistEpisodes){
            if(playlistEpisode.getPosition() != 0){
                playlistEpisode.deleteFromRealm();
            }
        }
        realm.commitTransaction();
        realm.close();
    }

    public void removeFromPlaylist(String enclosureUrl, String playlistId){
        Realm realm = getRealmInstance();

        PlaylistEpisode playlistEpisode = realm.where(PlaylistEpisode.class).contains("enclosureUrl",enclosureUrl).contains("playlistId",playlistId).findFirst();
        if(playlistEpisode != null){
            realm.beginTransaction();
            playlistEpisode.deleteFromRealm();
            realm.commitTransaction();
        }
        realm.close();
    }

    public void updatePlaylistEpisodePosition(String id, int position){
        Realm realm = getRealmInstance();
        PlaylistEpisode playlistEpisode = realm.where(PlaylistEpisode.class).contains("id",id).findFirst();
        if(playlistEpisode != null){
            realm.beginTransaction();
            playlistEpisode.setPosition(position);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void openPositionSpace(int position, String playlistId){
        Realm realm = getRealmInstance();

        RealmResults<PlaylistEpisode> playlistEpisodes = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).findAll();
        realm.beginTransaction();
        for(int i = position; i < playlistEpisodes.size(); i++){
            playlistEpisodes.get(i).setPosition(playlistEpisodes.get(i).getPosition()+1);
        }
        realm.commitTransaction();
        realm.close();
    }

    public void cleanUpPositions(String playlistId){
        Realm realm = getRealmInstance();

        RealmResults<PlaylistEpisode> playlistEpisodes = realm.where(PlaylistEpisode.class).contains("playlistId",playlistId).findAll().sort("position",Sort.ASCENDING);
        realm.beginTransaction();
        for(int i = 0; i < playlistEpisodes.size(); i++){
            playlistEpisodes.get(i).setPosition(i);
        }
        realm.commitTransaction();
        realm.close();
    }

    public void updateElapsedTime(String enclosureUrl, long time, long totalTime){
        Realm realm = getRealmInstance();

        Time elapsedTime = realm.where(Time.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(elapsedTime != null){
            realm.beginTransaction();
            elapsedTime.setElapsedTime(time);
            elapsedTime.setTotalTime(totalTime);
            realm.commitTransaction();
        }else {
            realm.beginTransaction();
            elapsedTime = realm.createObject(Time.class,UUID.randomUUID().toString());
            elapsedTime.setEnclosureUrl(enclosureUrl);
            elapsedTime.setElapsedTime(time);
            elapsedTime.setTotalTime(totalTime);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void createElapsedTime(Time time){
        Realm realm = getRealmInstance();

        if(time != null){
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(time);
            realm.commitTransaction();
        }
        realm.close();
    }

    public long getElapsedTimeFor(String enclosureUrl){
        Realm realm = getRealmInstance();

        Time time = realm.where(Time.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(time != null){
            long elapsedTimeValue = time.getElapsedTime();
            realm.close();
            return elapsedTimeValue;
        }
        realm.close();
        return 0;
    }

    public boolean isEpisodeListened(String enclosureUrl){
        Realm realm = getRealmInstance();
        Time time = realm.where(Time.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(time != null){
            long tolerance = (long)(time.getTotalTime() / (long)100) * (long)10;
            if(time.getElapsedTime()+tolerance >= time.getTotalTime()) {
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    public Time getElapsedTimeObjectFor(String enclosureUrl){
        Realm realm = getRealmInstance();

        Time time = realm.where(Time.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(time != null){
            time = realm.copyFromRealm(time);
            realm.close();
            return time;
        }
        realm.close();
        return null;
    }

    public ArrayList<Time> getUnfinishedEpisodes(){
        Realm realm = getRealmInstance();

        ArrayList<Time> timeArrayList = new ArrayList<>();
        RealmResults<Time> timeRealmResults = realm.where(Time.class).findAll();
        if(timeRealmResults != null){
            for(Time time : timeRealmResults){
                if(time.getElapsedTime() > 0 && time.getElapsedTime() < time.getTotalTime()-10000){ //minus 10 seconds
                    timeArrayList.add(realm.copyFromRealm(time));
                }
            }
            realm.close();
            return timeArrayList;
        }
        realm.close();
        return timeArrayList;
    }

    public void addUnfinished(Time time){
        Realm realm = getRealmInstance();
        if(time != null){
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(time);
            realm.commitTransaction();
        }
        realm.close();
    }

    public boolean addDownloadToQueue(Episode episode){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",episode.getEnclosureUrl()).findFirst();
        if(download == null){
            realm.beginTransaction();
            download = realm.createObject(Download.class,UUID.randomUUID().toString());
            download.setEnclosureUrl(episode.getEnclosureUrl());
            download.setImage(episode.getImage());
            download.setTitle(episode.getTitle());
            download.setDate(new Date().getTime());
            download.setEpisodeJson(new Gson().toJson(episode));
            realm.commitTransaction();

            realm.close();
            return true;
        }

        realm.close();
        return false;
    }


    public void setDownloadCompleted(String enclosureUrl, String localPath){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(download != null){
            realm.beginTransaction();
            download.setCompleted(true);
            download.setLocalPath(localPath);
            realm.commitTransaction();
        }

        realm.close();
    }

    public void setDownloadLocalPath(String enclosureUrl, String localPath){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(download != null){
            realm.beginTransaction();
            download.setLocalPath(localPath);
            realm.commitTransaction();
        }

        realm.close();
    }

    public ArrayList<Download> getDownloadQueue(){
        Realm realm = getRealmInstance();
        realm.refresh();
        ArrayList<Download> downloadsQueue = new ArrayList<>();
        RealmResults<Download> downloadQueueRealm = realm.where(Download.class).equalTo("completed",false).sort("date",Sort.ASCENDING).findAll();
        if(downloadQueueRealm != null){
            realm.beginTransaction();
            for(Download download : downloadQueueRealm){
                downloadsQueue.add(realm.copyFromRealm(download));
            }
            realm.commitTransaction();
        }
        realm.close();
        return downloadsQueue;
    }

    public ArrayList<Download> getDownloaded(){
        Realm realm = getRealmInstance();
        realm.refresh();
        ArrayList<Download> downloads = new ArrayList<>();
        RealmResults<Download> downloadQueueRealm = realm.where(Download.class).equalTo("completed",true).sort("date",Sort.ASCENDING).findAll();
        if(downloadQueueRealm != null){
            realm.beginTransaction();
            for(Download download : downloadQueueRealm){
                downloads.add(realm.copyFromRealm(download));
            }
            realm.commitTransaction();
        }
        realm.close();
        return downloads;
    }

    public void removeDownload(String enclosureUrl){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(download != null){
            realm.beginTransaction();
            download.deleteFromRealm();
            realm.commitTransaction();
        }

        realm.close();
    }

    public Download getDownload(String enclosureUrl){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(download != null){
            download = realm.copyFromRealm(download);
            realm.close();
            return download;
        }
        return null;
    }


    public int getDownloadStatus(String enclosureUrl){
        Realm realm = getRealmInstance();
        Download download = realm.where(Download.class).contains("enclosureUrl",enclosureUrl).findFirst();
        if(download != null && !download.isCompleted()){
            return Helper.STATE_QUEUED;
        }if(download != null && download.isCompleted()){
            return Helper.STATE_DOWNLOADED;
        }
        realm.close();
        return Helper.STATE_STREAM;
    }

    public void updatePodcastUpload(String url){
        Realm realm = getRealmInstance();
        PodcastUpload podcastUpload = realm.where(PodcastUpload.class).contains("url",url).findFirst();
        if(podcastUpload == null){
            realm.beginTransaction();
            podcastUpload = realm.createObject(PodcastUpload.class,UUID.randomUUID().toString());
            podcastUpload.setUrl(url);
            podcastUpload.setDate(new Date().getTime());
            realm.commitTransaction();
        }
        realm.close();
    }

    public PodcastUpload getPodcastUpload(String url){
        PodcastUpload podcastUpload = null;
        Realm realm = getRealmInstance();
        PodcastUpload podcastUploadRealm = realm.where(PodcastUpload.class).contains("url",url).findFirst();
        if(podcastUpload != null){
            podcastUpload = realm.copyFromRealm(podcastUploadRealm);
        }
        realm.close();
        return podcastUpload;
    }


    private void deleteEpisodes(String url){
        Realm realm = getRealmInstance();

        RealmResults<Episode> episodesRealm = realm.where(Episode.class).contains("url",url).findAll();
        if(episodesRealm != null){
            realm.beginTransaction();
            for(Episode episode : episodesRealm){
                episode.deleteFromRealm();
            }
            realm.commitTransaction();
        }
        realm.close();
    }

    private void deletePodcast(String url){
        Realm realm = getRealmInstance();

        Podcast podcastRealm = realm.where(Podcast.class).contains("url",url).findFirst();
        if(podcastRealm != null){
            realm.beginTransaction();
            podcastRealm.deleteFromRealm();
            realm.commitTransaction();
        }
        realm.close();
    }

    public void deleteEverything(){
        Realm realm = getRealmInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }


    public interface DatabaseUpdateListener{
        void onSuccess();
        void onFailure();
    }
}
