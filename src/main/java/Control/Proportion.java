package Control;

import Entity.IssueTicket;
import Entity.Release;
import Utils.ManageRelease;
import Utils.RetrieveJiraTickets;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Proportion {
    private static String projName = "BOOKKEEPER";
    public Proportion(){
    }

    public static Double computeProportion(List<IssueTicket> issues) throws IOException, ParseException {

        List<Double> proportions = new ArrayList<>(); //List of proportion values.

        //We are calculating the proportion value P for each ticket in the list
        for(IssueTicket issue : issues) {
            //P = (FV-IV)/(FV-OV)
            Double prop = (1.0)*(issue.getFixVersion().getId()-issue.getInjectedVersion().getId())/(issue.getFixVersion().getId()- issue.getOpeningVersion().getId());
            if(prop >= 1.0) {    //P cannot be less than 1
                proportions.add(prop);
            }

        }
        //Return the average among all the proportion values
        Double propSum = 0.0;
        for(Double prop : proportions) {
            propSum = propSum + prop;
        }
        return propSum/proportions.size();

    }






}
