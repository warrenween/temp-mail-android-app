package com.tempmail.models;

import com.google.gson.annotations.Expose;
import com.orm.SugarRecord;

/**
 * Created by Lotar on 25.12.2016.
 */

public class Ads extends SugarRecord {
    @Expose
    private String type;
    @Expose
    private String period;
    @Expose
    private String link;
    @Expose
    private String image_url;


    public Ads() {
    }

    public Ads(String type, String period) {
        this.type = type;
        this.period = period;
    }


    public Ads(String type, String period, String link, String image_url) {
        this.type = type;
        this.period = period;
        this.link = link;
        this.image_url = image_url;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
