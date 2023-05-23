package utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import entity.ReleaseCommits;
import entity.Release;
import org.eclipse.jgit.revwalk.RevCommit;

public class ReleaseCommitsUtil {

    //This private constructor is meant to hide the public one: utility classes do not have to be instantiated.
    private ReleaseCommitsUtil() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    public static RevCommit getLastCommit(List<RevCommit> commitsList) {
        RevCommit lastCommit;

            lastCommit = commitsList.get(0); //initialize lastCommit with the first commit of the list
            for(RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if(commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;

            }

        }
        return lastCommit;

    }


    public static ReleaseCommits getCommitsOfRelease(List<RevCommit> commitsList, Release release, Date firstDate) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        Date lastDate = release.getDate();

        for(RevCommit commit : commitsList) { //for each commit in commitsList (that is all the commits of the project) do
            Date commitDate = commit.getCommitterIdent().getWhen(); //get commitDate

            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if(commitDate.after(firstDate) && (commitDate.before(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit); //add commit to matchingCommits list (that is the list of commits of the release)
            }

        }
        RevCommit lastCommit = getLastCommit(matchingCommits); //get the last commit of the release
        return new ReleaseCommits(release, matchingCommits, lastCommit); //return a ReleaseCommits object

    }

    public static Release getReleaseOfCommit(RevCommit commit, List<ReleaseCommits> relCommAssociations) {
        for(ReleaseCommits relComm : relCommAssociations) {
            for(RevCommit c : relComm.getCommits()) {
                if(c.equals(commit)) {
                    return relComm.getRelease();
                }

            }

        }
        return null;

    }

}
