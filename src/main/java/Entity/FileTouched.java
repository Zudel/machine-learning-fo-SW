package Entity;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class FileTouched {
    private String pathname;
    private int loc;
    private int releaseIndex;
    private int nRev;
    private int nAuth;
    private int loc_Touched;
    private int cc;
    private int locm;
    private List<RevCommit> commits;
    private double avgChurn;

    private List<Integer> addedLinesList;
    private List<Integer> deletedLinesList;
    private String content;
    private boolean isBuggy;
    private int size;
    private int maxChurn;
    private int churn;
    private int maxLocAdded;
    private int locAdded;
    private double avgLocAdded;


    public FileTouched(String file, int id){
        this.pathname = file;
        this.loc = 0;
        this.releaseIndex = id;
    }

    public FileTouched(String file){
        this.pathname = file;
        this.loc = 0;
        this.avgLocAdded = 0;
        this.maxLocAdded = 0;
        this.locAdded = 0;
        this.churn = 0;
        this.releaseIndex = 0;
        this.nRev = 0;
        this.nAuth = 0;
        this.cc = 0;
        this.locm = 0;
        this.addedLinesList = new ArrayList<>();
        this.deletedLinesList = new ArrayList<>();

    }

    public FileTouched(String key, String value, Release release) {
        this.pathname = key;
        this.content = value;
        this.loc = 0;
        this.avgLocAdded = 0;
        this.maxLocAdded = 0;
        this.locAdded = 0;
        this.churn = 0;
        this.maxChurn = 0;
        this.releaseIndex = release.getId();
        this.size = 0;
        this.nRev = 0;
        this.nAuth = 0;
        this.cc = 0;
        this.locm = 0;
        this.addedLinesList = new ArrayList<>();
        this.deletedLinesList = new ArrayList<>();
        this.commits = new ArrayList<>();
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    /**
     * @param commits the commits to set
     */
    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }

    /**
     * @return the isBuggy
     */
    public boolean isBuggy() {
        return isBuggy;
    }

    /**
     * @param isBuggy the isBuggy to set
     */
    public void setBuggy(boolean isBuggy) {
        this.isBuggy = isBuggy;
    }

    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    
    


    public int getReleaseIndex() {
        return releaseIndex;
    }

    public void setReleaseIndex(int releaseIndex) {
        this.releaseIndex = releaseIndex;

    }

    public List<Integer> getAddedLinesList() {
        return addedLinesList;
    }

    /**
     * @param addedLinesList the addedLinesList to set
     */
    public void setAddedLinesList(List<Integer> addedLinesList) {
        this.addedLinesList = addedLinesList;
    }

    /**
     * @return the deletedLinesList
     */
    public List<Integer> getDeletedLinesList() {
        return deletedLinesList;
    }

    /**
     * @param deletedLinesList the deletedLinesList to set
     */
    public void setDeletedLinesList(List<Integer> deletedLinesList) {
        this.deletedLinesList = deletedLinesList;
    }

    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setChurn(int churn) {
        this.churn = churn;
    }

    /**
     * @return the maxChurn
     */
    public int getMaxChurn() {
        return maxChurn;
    }

    /**
     * @param maxChurn the maxChurn to set
     */
    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    /**
     * @return the avgChurn
     */
    public double getAvgChurn() {
        return avgChurn;
    }

    /**
     * @param avgChurn the avgChurn to set
     */
    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

        public String getPathname() {
            return pathname;
        }

        public void setPathname(String pathname) {
            this.pathname = pathname;
        }



        public int getNRev() {
            return nRev;
        }

        public void setNRev(int nRev) {
            this.nRev = nRev;
        }

        public int getNAuth() {
            return nAuth;
        }

        public void setNAuth(int nAuth) {
            this.nAuth = nAuth;
        }

        public int getLoc_Touched() {
            return loc_Touched;
        }

        public void setLoc_Touched(int loc_Touched) {
            this.loc_Touched = loc_Touched;
        }

        public int getCc() {
            return cc;
        }

        public void setCc(int cc) {
            this.cc = cc;
        }

        public int getLocm() {
            return locm;
        }

        public void setLocm(int locm) {
            this.locm = locm;
        }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocAdded() {
        return locAdded;
    }



    /**
     * @return the maxLocAdded
     */
    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    /**
     * @param maxLocAdded the maxLocAdded to set
     */
    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    /**
     * @return the avgLocAdded
     */
    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    /**
     * @param avgLocAdded the avgLocAdded to set
     */
    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }


    public int getChurn() {
        return churn;
    }
}
