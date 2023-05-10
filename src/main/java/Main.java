import Control.ComputeMetrics;
import Control.Proportion;
import Entity.FileTouched;
import Entity.IssueTicket;
import Entity.Release;
import Utils.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private int countConsistentIssue = 0;
    private int countInconsistentIssue = 0;

    public static void main(String[] args) throws Exception {
        RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
        DataManipulation dataManipulation = new DataManipulation();
        ManageRelease manageRelease = new ManageRelease();
        RetrieveGitInfoTicket retrieveGitInfoTicket = new RetrieveGitInfoTicket();
        String projNameB = "BOOKKEEPER";
        String projNameA = "AVRO";
        String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";

/*    List<IssueTicket> allIssueTicketsB = retrieveJiraTickets.retrieveTickets(projNameB);
    List<IssueTicket> allIssueTicketsA = retrieveJiraTickets.retrieveTickets(projNameA);
    List<IssueTicket> consistentIssueA = retrieveJiraTickets.retrieveConsistentTickets(allIssueTicketsA);
    List<IssueTicket> consistentIssueB = retrieveJiraTickets.retrieveConsistentTickets(allIssueTicketsB);
    allIssueTicketsA = retrieveJiraTickets.retrieveTickets(projNameA);
    int numConsistentIssueB = consistentIssueB.size();
    int numConsistentIssueA = consistentIssueA.size();
    int numIssueB = allIssueTicketsB.size();
    int numIssueA = allIssueTicketsA.size();
    double propB = (double) (numConsistentIssueB*100)/numIssueB;
    float propA = (float) (numConsistentIssueA*100)/numIssueA;

    System.out.println("Numero di ticket per BOOKKEEPER: " + numIssueB);
    System.out.println("Numero di ticket per AVRO: " + numIssueA);
    System.out.println("Numero di ticket consistenti per BOOKKEEPER: " + numConsistentIssueB);
    System.out.println("Numero di ticket consistenti per AVRO: " + numConsistentIssueA);
    System.out.println("Percentuale di ticket consistenti per BOOKKEEPER: " +propB);
    System.out.println("Percentuale di ticket consistenti per AVRO: " +propA);*/

        /*  calcolo il predicted IV per ogni ticket  */

    Proportion proportion = new Proportion();
    List<IssueTicket> issueTickets = retrieveJiraTickets.retrieveTickets(projNameB);
    List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
    List<IssueTicket> issueTicketListWithIV = proportion.proportionIncremental(issueTickets, consistentIssue);
    /*for(IssueTicket issueTicket : issueTicketListWithIV){
        System.out.println("Ticket: "+ issueTicket.getKey());
        System.out.println("IV: "+ issueTicket.getInjectedVersion().getId());
        System.out.println("OV: "+ issueTicket.getOpeningVersion().getId());
        System.out.println("FV: "+ issueTicket.getFixVersion().getId());
        ArrayList<String> avList = new ArrayList<>();
        for(Release release : issueTicket.getAvList()){
            avList.add(release.releaseName.toString());
        }
        System.out.println("AV LIST: "+ avList);

    }*/
        Repository repo = Git.open(new File(localPath + "/.git")).getRepository();
        Git git = new Git(repo);
        List<Release> releaseList = manageRelease.retrieveReleases(projNameB);
        List<Release> hashReleaseList = manageRelease.getHalfRelease(releaseList);
        List<RevCommit> allCommitsList = retrieveGitInfoTicket.retrieveAllCommits(git);
        List<ReleaseCommits> relCommAssociationsList = retrieveGitInfoTicket.getRelCommAssociations(allCommitsList, hashReleaseList);
        retrieveGitInfoTicket.getRelClassesAssociations(relCommAssociationsList); //associazioni tra release e classi toccate dai commit della release (per ogni release)
        List<FileTouched> javaClassesList = retrieveGitInfoTicket.labelClasses(relCommAssociationsList, issueTicketListWithIV); //lista di tutte le classi java toccate dai commit di ogni release (per ogni release) con label (buggy o non buggy)

        retrieveGitInfoTicket.assignCommitsToClasses(javaClassesList, allCommitsList, relCommAssociationsList);


        ComputeMetrics computeMetrics = new ComputeMetrics(retrieveGitInfoTicket, javaClassesList, allCommitsList, git);
        List<FileTouched>  javaClassesList2 = computeMetrics.doAllMetricsComputation();
        CsvWriter csvWriter = new CsvWriter(javaClassesList2);
        csvWriter.WriteOnCsv();
            }
}