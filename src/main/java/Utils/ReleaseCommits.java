package Utils;

import java.util.List;
import java.util.Map;

import Entity.Release;
import org.eclipse.jgit.revwalk.RevCommit;

public class ReleaseCommits {

    private Release release;
    private List<RevCommit> commits;
    private RevCommit lastCommit;
    private Map<String, String> javaClasses;  //Classes that were present when the release was deployed; they are represented by their name and their content

    public ReleaseCommits(Release release, List<RevCommit> commits, RevCommit lastCommit) {
        this.release = release;
        this.commits = commits;
        this.lastCommit = lastCommit;
        this.javaClasses = null;

    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }


    public List<RevCommit> getCommits() {
        return commits;
    }


    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }


    public RevCommit getLastCommit() {
        return lastCommit;
    }


    public void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }


    public Map<String, String> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(Map<String, String> javaClasses) {
        this.javaClasses = javaClasses;
    }

}