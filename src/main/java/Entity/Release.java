package Entity;

import Utils.DataManipulation;
import Utils.ManageRelease;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        DataManipulation dataManipulation = new DataManipulation();
        String data = dataManipulation.convertDate(date);
        this.date = date;
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
        DataManipulation dataManipulation = new DataManipulation();
        String data = dataManipulation.convertDate(date);
        Date date = dataManipulation.convertStringToDate(data);
        return date;
    }

    public void setId(int id) {
        this.releaseidex = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }



}
