package flows;

import control.Proportion;
import entity.IssueTicket;
import utils.RetrieveJiraTickets;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ProportionFlow {
    private List<IssueTicket> issueTickets;
    private List<IssueTicket> consistentIssue;
    private List<IssueTicket> issueTicketListWithIV;


    public ProportionFlow(RetrieveJiraTickets retrieveJiraTickets, String projNameB) throws IOException, ParseException {
        Proportion proportion = new Proportion();
        this.issueTickets = retrieveJiraTickets.retrieveTickets(projNameB);
        this.consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
        this.issueTicketListWithIV = proportion.proportionIncremental(issueTickets, consistentIssue);
    }

    public List<IssueTicket> getIssueTicketsWithIV() {
        return issueTicketListWithIV;
    }




}
