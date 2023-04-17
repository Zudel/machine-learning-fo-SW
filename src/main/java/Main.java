import Entity.IssueTicket;
import Entity.Release;
import Utils.ManageRelease;
import Utils.RetrieveJiraTickets;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Main {
    private static String projName = "BOOKKEEPER";
public static void main(String[] args) throws IOException, ParseException {
    RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
    List<IssueTicket> issueTickets = retrieveJiraTickets.retrieveTickets("BOOKKEEPER");
    //stampo la lista di ticket
    /*for (IssueTicket issueTicket : issueTickets) {
        System.out.println(issueTicket.getKey());
        System.out.println(issueTicket.getInjectedVersion());
        System.out.println(issueTicket.getFixVersion());
        System.out.println(issueTicket.getIvDate());
        System.out.println(issueTicket.getOpDate());
        System.out.println(issueTicket.getFxDate());
        System.out.println(issueTicket.getAvList());
        System.out.println("-------------------------------------------------");
    }*/
    ManageRelease manageRelease = new ManageRelease();
    List<Release> releases = manageRelease.retrieveReleases(projName); //lista delle release ordinate per data di rilascio crescente (dal più vecchio al più recente)
    //stampo la lista di release
    for (Release release : releases) {
        System.out.println(release.getReleaseName()+" index: "+ release.getId() + " Date: " + release.getDate());
    }
}
}
