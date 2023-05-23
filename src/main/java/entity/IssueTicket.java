package entity;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class IssueTicket {
    private String key;
    private Release injectedVersion;
    private Release fixVersion;
    private Release openingVersion;
    private List<Release> avList;

    public IssueTicket(String key, Release iv, Release fv, Release ov) {
        this.key = key;
        this.injectedVersion = iv;
        this.fixVersion = fv;
        this.openingVersion = ov;
        this.avList = avList;

    }


    //metodi get e set
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Release fixVersion) {
        this.fixVersion = fixVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }

    public List<Release> getAvList() {
        return avList;
    }

    public void setAvList(List<Release> avList) {
        this.avList = avList;
    }

    public Date getIvDate() throws ParseException {
        return injectedVersion.getDate();
    }

    public Date getFxDate() throws ParseException {
        return fixVersion.getDate();
    }

    public Date getOpDate() throws ParseException {
        return openingVersion.getDate();
    }







}
