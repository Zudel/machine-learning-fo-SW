import Control.Proportion;
import Entity.IssueTicket;
import Entity.Release;
import Utils.ManageRelease;
import Utils.RetrieveJiraTickets;


import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private int countConsistentIssue = 0;
    private int countInconsistentIssue = 0;
public static void  main(String[] args) throws IOException, ParseException {
    RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
    String projNameB = "BOOKKEEPER";
    String projNameA = "AVRO";


    //calcolo la proporzione

    //calcolo il predicted IV per ogni ticket
    List<IssueTicket> allIssueTicketsB = retrieveJiraTickets.retrieveTickets(projNameB);
    List<IssueTicket> allIssueTicketsA = retrieveJiraTickets.retrieveTickets(projNameA);
    List<IssueTicket> consistentIssueA = retrieveJiraTickets.retrieveConsistentTickets(allIssueTicketsA);
    List<IssueTicket> consistentIssueB = retrieveJiraTickets.retrieveConsistentTickets(allIssueTicketsB);
    allIssueTicketsA = retrieveJiraTickets.retrieveTickets(projNameA);
    int numConsistentIssueB = consistentIssueB.size();
    int numConsistentIssueA = consistentIssueA.size();
    int numIssueB = allIssueTicketsB.size();
    int numIssueA = allIssueTicketsA.size();

    System.out.println("Numero di ticket consistenti per BOOKKEEPER: " + numConsistentIssueB);
    System.out.println("Numero di ticket consistenti per AVRO: " + numConsistentIssueA);
    System.out.println("Percentuale di ticket consistenti per BOOKKEEPER: " +(double) (numConsistentIssueB/numIssueB)*100);
    System.out.println("Percentuale di ticket consistenti per AVRO: " +(double) ((numConsistentIssueA/numIssueA)*100));


}
}
