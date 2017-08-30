package com.tempmail.api.models.answers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Lotar on 25.12.2016.
 */

public class Mails implements Serializable {
    @SerializedName("mail_id")
    @Expose
    String mailId;

    @SerializedName("mail_from")
    @Expose
    String mailFrom;


    @SerializedName("mail_subject")
    @Expose
    String mailSubject;


    @SerializedName("mail_timestamp")
    @Expose
    double mailTimestamp;


    @SerializedName("mail_html")
    @Expose
    String mailHtml;

    public String getMailId() {
        return mailId;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public double getMailTimestamp() {
        return mailTimestamp;
    }

    public String getMailHtml() {
        return mailHtml;
    }



}
