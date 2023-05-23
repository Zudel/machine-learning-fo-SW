package control;

import entity.IssueTicket;
import entity.Release;
import utils.EnumProjects;
import static utils.ManageRelease.retrieveReleases;
import utils.RetrieveJiraTickets;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static java.lang.Math.*;

public class Proportion {
    private String projName = "BOOKKEEPER";
    private static int  thresholdClodStart = 5;

    public Double computeProportion(List<IssueTicket> issues){
        double propValue;
        List<Double> proportions = new ArrayList<>(); //List of proportion values.
        for (IssueTicket issue : issues) {
            //P = (FV-IV)/(FV-OV)
            double prop;
            if (issue.getFixVersion().getId() - issue.getOpeningVersion().getId() <= 0) {
                prop = (1.0) * (issue.getFixVersion().getId() - issue.getInjectedVersion().getId()) / (1.0); //if the denominator is 0, we set FV - OV to 1
            }
            else {
                prop = (1.0) * (issue.getFixVersion().getId() - issue.getInjectedVersion().getId()) / (issue.getFixVersion().getId() - issue.getOpeningVersion().getId());

            }
            proportions.add(prop); //add the proportion value to the list of proportion values for each issue ticket

        }
        //Return the average among all the proportion values
        Double propSum = 0.0;
        for (Double prop : proportions) {
            propSum = propSum + prop;
        }
        propValue = propSum / proportions.size();
        return propValue; //return the average among all the proportion values
    }

    //Proportion_ColdStart method
    public double proportionColdstart() throws IOException, ParseException {
        Map<EnumProjects, Double> enumMap = new EnumMap<>(EnumProjects.class);
        RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
        List<IssueTicket> issueTickets;

        for (EnumProjects enumProjects : EnumProjects.values()) {
            issueTickets = retrieveJiraTickets.retrieveTickets(enumProjects.toString());
                List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
                enumMap.put(enumProjects, computeProportion(consistentIssue));
        }
        //compute the median of the proportion values of all the projects

        if (enumMap.size() % 2 == 0) {
             return (enumMap.get(EnumProjects.values()[enumMap.size() / 2]) + enumMap.get(EnumProjects.values()[enumMap.size() / 2 - 1])) / 2;
        }
        else {
            return enumMap.get(EnumProjects.values()[enumMap.size() / 2]);
        }



    }
    /**
     * For each defect, we computed the IV as IV = (FV − OV ) ∗ P_ColdStart. If FV
     * equals OV, then IV equals FV. However, recall we excluded defects that were not
     * post-release. Therefore, we set FV − OV equal to 1 to assure IV is not equal to FV.
     */
    public void computePredictedIVWithColdstart(IssueTicket issue, List<Release> releases) throws ParseException, IOException {
        double pColdStart = proportionColdstart(); //compute the P_ColdStart value
        int predictedIV;

            if(issue.getInjectedVersion().getReleaseName().equals("N/A") && issue.getFixVersion().getId() != 0){
                if(issue.getFixVersion().getId() - issue.getOpeningVersion().getId() == 0)
                    predictedIV= 1;
                else
                    predictedIV = (int) floor((issue.getFixVersion().getId() - issue.getOpeningVersion().getId()) * pColdStart);

                issue.getInjectedVersion().setId(predictedIV);
                issue.getInjectedVersion().setReleaseName(releases.get(predictedIV).getReleaseName());
                issue.getInjectedVersion().setDate(releases.get(predictedIV).getDate());

            }


    }
    /**Proportion_Increment:
     (i) For each version R, we computed P_Increment as the average P among defects
     fixed in versions 1 to R-1.
     (ii) We used the P_ColdStart for P_Increment values containing less than five defects
     on average.
     (iii) For each defect in each version, we computed the IV as IV = (FV − OV ) ∗
     P_Increment . If FV equals OV, then IV equals FV. However, recall we excluded
     defects that were not post-release. Therefore, we set FV − OV equal to 1 to assure
     IV is not equal to FV.
     (iv) For each defect, we label each version before the IV as not affected.We label each
     version from the IV to the FV as affected. The FV is labeled not affected.
     */
    public List<IssueTicket> proportionIncremental(List<IssueTicket> allIssueTickets, List<IssueTicket> issueTicketsWithIV) throws ParseException, IOException {
        double pIncrement;
        double pValueColdStart = proportionColdstart(); //compute the P_ColdStart value
        int predictedIV;
        int countWithIV;
        List<IssueTicket> predictedIssueTickets = new ArrayList<>();

        for (IssueTicket issue : allIssueTickets) {
            countWithIV = 0;
            for (IssueTicket issueWithIV : issueTicketsWithIV) { //count the number of tickets with IV in the list of all tickets (to compute the P_increment)
                if(issue.getFixVersion().getDate() != null && issue.getInjectedVersion().getId() != issue.getFixVersion().getId() && issue.getFixVersion().getDate().after(issueWithIV.getFixVersion().getDate()) && issue.getInjectedVersion().getReleaseName().equals("N/A"))
                    countWithIV++;
            }
            if(countWithIV < thresholdClodStart) //if the number of tickets with IV is less than the threshold, we use the P_ColdStart value
                pIncrement = pValueColdStart;
            else //otherwise, we compute the P_increment value
                pIncrement = computeProportion(issueTicketsWithIV);
            if(issue.getInjectedVersion().getReleaseName().equals("N/A")) {
                predictedIV = (int) floor((issue.getFixVersion().getId() - issue.getOpeningVersion().getId()) * pIncrement); //compute the IV for each ticket in the list and take the floor value
                if(predictedIV == 0)
                    predictedIV = 1;
                issue.getInjectedVersion().setId(predictedIV); //compute the IV for each ticket in the list and take the floor value
                issue.getInjectedVersion().setReleaseName(issue.getFixVersion().getReleaseName()); //set the release name of the relative IV index
                issue.getInjectedVersion().setDate(issue.getFixVersion().getDate()); //set the date of the relative IV index

            }
            labelAffected(issue); //label each version after the IV and FV (exclused) as affected
            predictedIssueTickets.add(issue);

        }
        return predictedIssueTickets;
    }

    public void labelAffected(IssueTicket issue) throws ParseException, IOException {
        List<Release> releases = retrieveReleases(projName);
        for(Release release : releases){
            if(release.getId() >= issue.getInjectedVersion().getId() && release.getId() < issue.getFixVersion().getId())
                issue.getAvList().add(release);
        }

    }
}
