package Entity;


import java.text.ParseException;
import java.util.*;

public class Release {
    public String releaseName;
    public int releaseidex;
    public Date date;
    public Release(String releaseName, Date date) {
        this.releaseName = releaseName;
        this.date = date;
    }

    public Release(int id, String name, Date date) throws ParseException {
        this.releaseidex = id;
        this.releaseName = name;
        this.date = date;
    }

    public Release(String fixVersion) {
        this.releaseName = fixVersion;
    }

    public Release(String releaseName, Date date, int ovIndex) {
        this.releaseName = releaseName;
        this.date = date;
        this.releaseidex = ovIndex;
    }

    public Release() {

    }


    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public int getId() {
        return releaseidex;
    }


    public Date getDate() throws ParseException {
        return date;
    }

    public void setId(int id) {
        this.releaseidex = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }



}
