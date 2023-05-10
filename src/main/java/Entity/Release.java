package Entity;


import java.text.ParseException;
import java.util.*;

public class Release {
    public String releaseName;
    public int releaseidex;
    public Date date;
    List<FileTouched> buggyFiles;
    List<FileTouched> files;

    public Release(String releaseName, Date date) {
        this.releaseName = releaseName;
        this.date = date;
        this.buggyFiles = new ArrayList<>();
        this.files = new ArrayList<>();
        this.releaseidex = 0;

    }

    public Release(int id, String name, Date date) throws ParseException {
        this.releaseidex = id;
        this.releaseName = name;
        this.date = date;
        this.buggyFiles = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public Release(String releaseName, Date date, int ovIndex) {
        this.releaseName = releaseName;
        this.date = date;
        this.releaseidex = ovIndex;
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

    public List<FileTouched> getBuggyFiles() {
        return buggyFiles;
    }

    public void setBuggyFiles(List<FileTouched> buggyFiles) {
        this.buggyFiles = buggyFiles;
    }

    public List<FileTouched> getFiles() {
        return files;
    }

    public void setFiles(List<FileTouched> files) {
        this.files = files;
    }

    public void addFile(FileTouched file){
        this.files.add(file);
    }

    public void addBuggyFile(FileTouched file){
        this.buggyFiles.add(file);
    }




}
