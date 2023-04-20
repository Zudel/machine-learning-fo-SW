package Control;

import Entity.IssueTicket;
import Entity.Release;
import Utils.EnumProjects;
import Utils.RetrieveJiraTickets;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class Proportion {
    private String projName = "BOOKKEEPER";

    public Proportion() {
    }

    public Double computeProportion(List<IssueTicket> issues) throws IOException, ParseException {
        double propValue = 0.0;
        List<Double> proportions = new ArrayList<>(); //List of proportion values.
        for (IssueTicket issue : issues) {

            //P = (FV-IV)/(FV-OV)
            double prop;
            if (issue.getFixVersion().getId() - issue.getOpeningVersion().getId() == 0) {
                prop = (1.0) * (issue.getFixVersion().getId() - issue.getInjectedVersion().getId()) / (1.0); //if the denominator is 0, we set FV - OV to 1
            } else {
                prop = (1.0) * (issue.getFixVersion().getId() - issue.getInjectedVersion().getId()) / (issue.getFixVersion().getId() - issue.getOpeningVersion().getId());

            }
            if (prop >= 1.0) {    //P cannot be less than 1
                proportions.add(prop);
            }

        }
        //Return the average among all the proportion values
        Double propSum = 0.0;
        for (Double prop : proportions) {
            propSum = propSum + prop;
        }
        propValue = propSum / proportions.size();


        return propValue;
    }

    //Proportion_ColdStart method
    public double proportionColdstart() {
        Map<EnumProjects, Double> enumMap = new HashMap<>();
        double MedianProportionValue = 0.0;
        List<IssueTicket> issueTickets;
        for (EnumProjects enumProjects : EnumProjects.values()) {
            try {
                RetrieveJiraTickets retrieveJiraTickets = new RetrieveJiraTickets();
                issueTickets = retrieveJiraTickets.retrieveTickets(enumProjects.toString());
                List<IssueTicket> consistentIssue = retrieveJiraTickets.retrieveConsistentTickets(issueTickets);
                enumMap.put(enumProjects, computeProportion(consistentIssue));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
    public List<IssueTicket> computePredictedIVWithColdstart(List<IssueTicket> issueTicket, List<Release> releases) throws ParseException {
        double P_ColdStart = proportionColdstart(); //compute the P_ColdStart value
        int predictedIV;
        //stampa tutte le release
        for (Release release : releases) {
            System.out.println("Release: " + release.getReleaseName() + " id: " + release.getId() + " date: " + release.getDate());
        }
        System.out.println("P_ColdStart: " + P_ColdStart);
        List<IssueTicket> issueTicketsWithIv = new ArrayList<>();
        issueTicketsWithIv.addAll(issueTicket);
        /*for (IssueTicket Ticket : issueTicket) {
            System.out.println("key:" + Ticket.getKey());
            System.out.println("iv: " + Ticket.getInjectedVersion().getId());
            System.out.println("fv: "+Ticket.getFixVersion().getId());
            System.out.println("ov: "+Ticket.getOpeningVersion().getId());
            System.out.println("iv date" +Ticket.getIvDate());
            System.out.println("ope date: "+ Ticket.getOpDate());
            System.out.println("fix date: "+Ticket.getFxDate());
            System.out.println("-------------------------------------------------");
        }*/
        for(IssueTicket issue : issueTicketsWithIv){
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
        return issueTicketsWithIv; //return the list of tickets with the predicted IV



    }
}
