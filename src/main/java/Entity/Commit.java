package Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit {
    private String commitId;
    private Date commitDate;
    private Release release;
    private String author;
    private int releaseIndex;
    private List<FileTouched> changedFileToucheds; //lista di file modificati nel commit

    public Commit(String cid, String author, Date authorDate, Release release){
        this.commitId = cid;
        this.author = author;
        this.commitDate = authorDate;
        this.release = release;
        this.changedFileToucheds = new ArrayList<>();


    }


    public String getAuthor() {
            return author;
        }



        public String getCommitSha(){

            String[] parts = this.commitId.split(" ");
            return parts[1];
        }
        public String getCommitId() {
            return commitId;
        }
        public void setCommitId(String commitId) {
            this.commitId = commitId;
        }
        public Date getCommitDate() {
            return commitDate;
        }
        public void setCommitDate(Date commitDate) {
            this.commitDate = commitDate;
        }


        public Release getRelease() {
            return release;
        }
        public void setRelease(Release release) {
            this.release = release;
        }
        public void setAuthor(String author) {
            this.author = author;
        }

        public int getReleaseIndex() {
            return releaseIndex;
        }

        public void setReleaseIndex(int releaseIndex) {
            this.releaseIndex = releaseIndex;
        }

        public List<FileTouched> getChangedFiles() {
            return changedFileToucheds;
        }

        public void addAllFilesInChangedFiles(List<FileTouched> item) {
            this.changedFileToucheds.addAll(item);
        }


    public void addFileTouched(FileTouched fileTouched) {
        this.changedFileToucheds.add(fileTouched);
    }
}

