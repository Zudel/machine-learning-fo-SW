import Control.Proportion;
import Entity.Commit;
import Entity.IssueTicket;
import Entity.Release;
import Utils.EnumProjects;
import Utils.RetrieveGitInfoTicket;
import Utils.RetrieveJiraTickets;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private int countConsistentIssue = 0;
    private int countInconsistentIssue = 0;
public static void  main(String[] args) throws Exception {
    RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
    String projNameB = "BOOKKEEPER";
    String projNameA = "AVRO";
    //calcolo la proporzione

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

    /*Proportion proportion = new Proportion();
    List<IssueTicket> issueTickets = retrieveJiraTickets.retrieveTickets(projNameB);
    List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
    proportion.proportionIncremental(issueTickets, consistentIssue);*/
    /*for(IssueTicket issueTicket : issueTickets){
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
    RetrieveGitInfoTicket retrieveGitInfoTicket = new RetrieveGitInfoTicket();
    List<Commit> commitList = retrieveGitInfoTicket.gitCommitClasses();
    //stampa tutti i campi di un commit in commitList (commitList è una lista di commit) e stampa anche il numero di commit totali
    for (Commit commit: commitList){
        System.out.println("Commit: "+ commit.getCommitId());
        System.out.println("Commit date: "+ commit.getCommitDate().toString());
        System.out.println("Commit author: "+ commit.getAuthor());
        System.out.println("Commit release: "+ commit.getRelease().getReleaseName());
        System.out.println("Commit release index: "+ commit.getRelease().getId());
        for (int i = 0; i < commit.getChangedFiles().size(); i++){
            System.out.println("Commit changed file toucheds: "+ commit.getChangedFiles().get(i).getPathname());
        }



    }

}
}
