package Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Entity.ReleaseCommits;
import Entity.FileTouched;
import Entity.Release;
import org.eclipse.jgit.revwalk.RevCommit;
public class JavaClassUtil {

    //This private constructor is meant to hide the public one: utility classes do not have to be instantiated.
    private JavaClassUtil() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    public static List<FileTouched> buildAllJavaClasses(List<ReleaseCommits> relCommAssociations) {
        List<FileTouched> javaClasses = new ArrayList<>();
        for(ReleaseCommits relComm : relCommAssociations) {
            for(Map.Entry<String, String> entryMap : relComm.getJavaClasses().entrySet()) {
                javaClasses.add(new FileTouched(entryMap.getKey(), entryMap.getValue(), relComm.getRelease()));

            }
        }
        return javaClasses;
    }

    public static void updateJavaClassBuggyness(List<FileTouched> javaClasses, String className, Release iv, Release fv) {
        //fv is related to the single commit, not to the ticket
        for(FileTouched javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to a version v such that iv <= v < fv, then javaClass is buggy
            if(javaClass.getPathname().equals(className) && javaClass.getReleaseIndex() >= iv.getId() && javaClass.getReleaseIndex()< fv.getId()) {
                javaClass.setBuggy(true);

            }

        }

    }

    public static void updateJavaClassCommits(List<FileTouched> javaClasses, String className, Release associatedRelease, RevCommit commit) {

        for(FileTouched javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to the same release of commit, then add commit to javaClass.commits
            if(javaClass.getPathname().equals(className) && javaClass.getReleaseIndex() == associatedRelease.getId() && !javaClass.getCommits().contains(commit)) {
                javaClass.getCommits().add(commit);
            }

        }

    }

}