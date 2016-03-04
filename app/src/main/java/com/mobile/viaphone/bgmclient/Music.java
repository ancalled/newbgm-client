package com.mobile.viaphone.bgmclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Music {

    private String acrid;
    private String title;
    private String label;
    private Long duration;
    private Date releaseDate;
    private String album;
    private List<String> genres;
    private List<String> artists;
    private Long playOffset;
    private String isrcCode;
    private String upcCode;

    public String getAcrid() {
        return acrid;
    }

    public void setAcrid(String acrid) {
        this.acrid = acrid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void addGenre(String genre) {
        if (this.genres == null) this.genres = new ArrayList<String>();
        this.genres.add(genre);
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public void addArtist(String artist) {
        if (this.artists == null) this.artists = new ArrayList<String>();
        this.artists.add(artist);
    }

    public Long getPlayOffset() {
        return playOffset;
    }

    public void setPlayOffset(Long playOffset) {
        this.playOffset = playOffset;
    }

    public String getIsrcCode() {
        return isrcCode;
    }

    public void setIsrcCode(String isrcCode) {
        this.isrcCode = isrcCode;
    }

    public String getUpcCode() {
        return upcCode;
    }

    public void setUpcCode(String upcCode) {
        this.upcCode = upcCode;
    }
}
