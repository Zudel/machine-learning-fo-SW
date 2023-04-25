import Control.Proportion;
import Entity.IssueTicket;
import Utils.EnumProjects;
import Utils.RetrieveJiraTickets;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;
public class Main {

    private int countConsistentIssue = 0;
    private int countInconsistentIssue = 0;
public static void  main(String[] args) throws IOException, ParseException {
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
    Proportion proportion = new Proportion();
    List<IssueTicket> issueTickets = retrieveJiraTickets.retrieveTickets(projNameB);
    List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);

    proportion.proportionIncremental(issueTickets, consistentIssue);








}
}
