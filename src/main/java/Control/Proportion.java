package Control;

import Entity.IssueTicket;
import Entity.Release;
import Utils.EnumProjects;
import Utils.ManageRelease;
import Utils.RetrieveJiraTickets;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static java.lang.Math.*;

public class Proportion {
    private String projName = "BOOKKEEPER";
    private static int  THRESHOLDCOLDSTART = 5;
    private double MedianProportionValue;

    public Double computeProportion(List<IssueTicket> issues) throws IOException, ParseException {
        double propValue = 0.0;
        List<Double> proportions = new ArrayList<>(); //List of proportion values.
        for (IssueTicket issue : issues) {
            //P = (FV-IV)/(FV-OV)
            double prop;
            if (issue.fixVersion.getId() - issue.openingVersion.getId() <= 0) {
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
        Map<EnumProjects, Double> enumMap = new HashMap<>();
        RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
        List<IssueTicket> issueTickets;

        for (EnumProjects enumProjects : EnumProjects.values()) {
            issueTickets = retrieveJiraTickets.retrieveTickets(enumProjects.toString());
                List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
                enumMap.put(enumProjects, computeProportion(consistentIssue));
        }
        //compute the median of the proportion values of all the projects

        if (enumMap.size() % 2 == 0) {
             return MedianProportionValue = (enumMap.get(EnumProjects.values()[enumMap.size() / 2]) + enumMap.get(EnumProjects.values()[enumMap.size() / 2 - 1])) / 2;
        }
        else {
            return MedianProportionValue = enumMap.get(EnumProjects.values()[enumMap.size() / 2]);
        }



    }
    /**
     * For each defect, we computed the IV as IV = (FV − OV ) ∗ P_ColdStart. If FV
     * equals OV, then IV equals FV. However, recall we excluded defects that were not
     * post-release. Therefore, we set FV − OV equal to 1 to assure IV is not equal to FV.
     */
    public void computePredictedIVWithColdstart(IssueTicket issue, List<Release> releases) throws ParseException, IOException {
        double P_ColdStart = proportionColdstart(); //compute the P_ColdStart value
        int predictedIV;
        System.out.println("P_ColdStart: " + P_ColdStart);
            if(issue.injectedVersion.getReleaseName().equals("N/A") && issue.fixVersion.getId() != 0){
                if(issue.fixVersion.getId() - issue.openingVersion.getId() == 0)
                    predictedIV= 1;
                else
                    predictedIV = (int) floor((issue.fixVersion.getId() - issue.openingVersion.getId()) * P_ColdStart); //compute the IV for each ticket in the list and take the floor value

                issue.injectedVersion.setId(predictedIV); //compute the IV for each ticket in the list and take the floor value
                issue.injectedVersion.setReleaseName(releases.get(predictedIV).getReleaseName()); //set the release name of the relative IV index               issue.injectedVersion.setDate(issue.injectedVersion.getDate());
                issue.injectedVersion.setDate(releases.get(predictedIV).getDate()); //set the date of the relative IV index

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
        double P_increment;
        double P_ColdStart = proportionColdstart(); //compute the P_ColdStart value
        int predictedIV;
        int countWithIV;
        List<IssueTicket> predictedIssueTickets = new ArrayList<>();


        System.out.println("P_ColdStart: " + P_ColdStart);
        for (IssueTicket issue : allIssueTickets) {
            countWithIV = 0;
            for (IssueTicket issueWithIV : issueTicketsWithIV) { //count the number of tickets with IV in the list of all tickets (to compute the P_increment)
                if(issue.fixVersion.getDate() != null && issue.fixVersion.getDate().after(issueWithIV.fixVersion.getDate()) && issue.injectedVersion.getReleaseName().equals("N/A"))
                    countWithIV++;
            }
            if(countWithIV < THRESHOLDCOLDSTART) //if the number of tickets with IV is less than the threshold, we use the P_ColdStart value
                P_increment = P_ColdStart;
            else //otherwise, we compute the P_increment value
                P_increment = computeProportion(issueTicketsWithIV);
            if(issue.injectedVersion.getReleaseName().equals("N/A")) {
                predictedIV = (int) floor((issue.fixVersion.getId() - issue.openingVersion.getId()) * P_increment); //compute the IV for each ticket in the list and take the floor value
                if(predictedIV == 0)
                    predictedIV = 1;
                issue.injectedVersion.setId(predictedIV); //compute the IV for each ticket in the list and take the floor value
                issue.injectedVersion.setReleaseName(issue.fixVersion.getReleaseName()); //set the release name of the relative IV index
                issue.injectedVersion.setDate(issue.fixVersion.getDate()); //set the date of the relative IV index

               /* System.out.println("Key: " + issue.key);
                System.out.println("predictedIV: " + predictedIV);
                System.out.println("IV: " + issue.injectedVersion.getId());
                System.out.println("FV: " + issue.fixVersion.getId());
                System.out.println("OV: " + issue.openingVersion.getId());
                System.out.println("P_increment: " + P_increment);*/
            }
            labelAffected(issue); //label each version after the IV and FV (exclused) as affected
            if(!(issue.injectedVersion.getId() == issue.fixVersion.getId())){
                //scartalo dalla lista allIssueTickets
                predictedIssueTickets.add(issue);
            }


        }
        return predictedIssueTickets;
    }

    public void labelAffected(IssueTicket issue) throws ParseException, IOException {
        ManageRelease manageRelease = new ManageRelease();
        List<Release> releases = manageRelease.retrieveReleases(projName);
        issue.avList = new ArrayList<>();
        for(Release release : releases){
            if(release.getId() >= issue.injectedVersion.getId() && release.getId() < issue.fixVersion.getId())
                issue.avList.add(release);
        }

    }
}
