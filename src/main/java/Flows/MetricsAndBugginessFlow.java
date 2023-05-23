package Flows;

import Control.ComputeMetrics;
import Entity.ReleaseCommits;
import Entity.FileTouched;
import Entity.IssueTicket;
import Entity.Release;
import Utils.CsvUtils;
import Utils.RetrieveGitInfoTicket;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class MetricsAndBugginessFlow {
    private  List<FileTouched> javaClassesList2;

    public MetricsAndBugginessFlow(Repository repo, List<IssueTicket> issueTicketListWithIV, List<Release> halfReleaseList, String projectName) throws IOException, ParseException, GitAPIException {
        Git git = new Git(repo);

        RetrieveGitInfoTicket retrieveGitInfoTicket = new RetrieveGitInfoTicket(repo, git);
        List<RevCommit> allCommitsList = retrieveGitInfoTicket.retrieveAllCommits(git);
        List<ReleaseCommits> relCommAssociationsList = retrieveGitInfoTicket.getRelCommAssociations(allCommitsList, halfReleaseList); //associazioni tra release e commit (per ogni release) (commit di ogni release)
        retrieveGitInfoTicket.getRelClassesAssociations(relCommAssociationsList); // settiamo per ogni rrelease tutte le classi toccate nei commit di quella release
        List<FileTouched> javaClassesList = retrieveGitInfoTicket.labelClasses(relCommAssociationsList, issueTicketListWithIV); //lista di tutte le classi java toccate dai commit di ogni release (per ogni release) con label (buggy o non buggy)
        retrieveGitInfoTicket.assignCommitsToClasses(javaClassesList, allCommitsList, relCommAssociationsList);

        ComputeMetrics computeMetrics = new ComputeMetrics(retrieveGitInfoTicket, javaClassesList, allCommitsList, git);
        javaClassesList2 = computeMetrics.doAllMetricsComputation();
        CsvUtils.writeOnCsv(javaClassesList2, projectName);
    }

    public List<FileTouched> getJavaClassesList2() {
        return javaClassesList2;
    }
}
