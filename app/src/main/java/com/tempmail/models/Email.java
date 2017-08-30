package com.tempmail.models;

import com.orm.SugarRecord;

/**
 * Created by Lotar on 25.12.2016.
 */

public class Email extends SugarRecord {
    private String eid;
    private boolean checked;


    public Email() {
    }

    public Email(String eid, boolean checked) {
        this.eid = eid;
        this.checked = checked;
    }


    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
