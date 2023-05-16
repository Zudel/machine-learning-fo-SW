import Entity.FileTouched;
import Entity.Release;
import Flows.MetricsAndBugginessFlow;
import Flows.ProportionFlow;
import Flows.WekaFlow;
import Utils.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.util.List;

import static Utils.ManageRelease.getHalfRelease;
import static Utils.ManageRelease.retrieveReleases;

public class MainFlow {

    public static void main(String[] args) throws Exception {
        RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
        String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";
        Repository repo = Git.open(new File(localPath + "/.git")).getRepository();
        RetrieveProject project = new RetrieveProject(false); //true = AVRO false = BOOKKEEPER
        List<Release> releases = retrieveReleases(project.getProjName());
        List<Release> halfReleases = getHalfRelease(releases);
        ProportionFlow proportionFlow = new ProportionFlow(retrieveJiraTickets, project.getProjName());
        List<FileTouched> javaClasses = new MetricsAndBugginessFlow(repo, proportionFlow.getIssueTicketsWithIV(), halfReleases).getJavaClassesList2();


        WekaFlow wekaFlow = new WekaFlow(project, halfReleases, javaClasses);

    }

}