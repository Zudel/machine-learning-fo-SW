import entity.FileTouched;
import entity.Release;
import flows.MetricsAndBugginessFlow;
import flows.ProportionFlow;
import flows.WekaFlow;
import utils.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import java.io.File;
import java.util.List;

import static utils.ManageRelease.getHalfRelease;
import static utils.ManageRelease.retrieveReleases;

public class MainFlow {
    public static void main(String[] args) throws Exception {

        RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
        RetrieveProject project = new RetrieveProject(false); //true = AVRO false = BOOKKEEPER
        String localPath = project.getProjDirName();
        Repository repo = Git.open(new File(localPath + "/.git")).getRepository();
        List<Release> releases = retrieveReleases(project.getProjName());
        List<Release> halfReleases = getHalfRelease(releases);
        ProportionFlow proportionFlow = new ProportionFlow(retrieveJiraTickets, project.getProjName());
        List<FileTouched> javaClasses = new MetricsAndBugginessFlow(repo, proportionFlow.getIssueTicketsWithIV(), halfReleases, project.getProjName()).getJavaClassesList2();
        //List<FileTouched> javaClasses = leggiCsv("C:\\Users\\Roberto\\Documents\\GitHub\\deliverable-ISW2\\", project.getProjName()+"-results_M1.csv" );
        new WekaFlow(project, halfReleases, javaClasses);
    }
}