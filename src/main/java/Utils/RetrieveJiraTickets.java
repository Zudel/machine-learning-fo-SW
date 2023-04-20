package Utils;

import Entity.IssueTicket;
import Entity.Release;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static Utils.JSONUtils.readJsonFromUrl;

/**
 * versions: data di rilascio delle versioni affette dal bug
 * fixVersions: data di rilascio delle versioni in cui il bug è stato risolto
 * */
public class RetrieveJiraTickets {
    private static String fields ="fields";
    private static String rel= "released";
    private static String relDate="releaseDate";
    private static String projName ="BOOKKEEPER";
    private static final String FIX="fixVersions";
    private static final String FIELDS="fields";
    private static final String RELEASEDATE="releaseDate";
    private static final String VERSION="versions";
    private static final String DATAPATH="yyyy-MM-dd";
    private String dataFormattata2Injected;
    private String key;
    private Release openingRelease = null;
    private JSONObject json2;
    private String dataFormattataResolved;
    private String fixVersion;
    private int ivIndex;
    private IssueTicket ticket;


    public List<IssueTicket> retrieveTickets(String projName) throws IOException, ParseException {
       String injectedVersion = null;
       String injectedVersionDate;
       Integer j, i = 0, total = 1;
        Date fvDate= null;
       List<IssueTicket> tickets = new ArrayList<>();
       List<Release> avList;
      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;

          String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                  + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                  + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created,fixVersions&startAt="
                  + i.toString() + "&maxResults=" + j.toString();

          json2 = readJsonFromUrl(url);
          JSONArray issues = json2.getJSONArray("issues"); //Get the JSONArray value associated with a issues.
          total = json2.getInt("total");
          int t = 0;
           for (; i < total && i < j; i++) {

                //estraggo tutte le informazioni che mi servono dal JSON dei report e le salvo in variabili locali
               JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
               JSONArray listAV = fields.getJSONArray("versions");
               //System.out.println(issues);
                if(!listAV.isEmpty() && issues.getJSONObject(i% 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).has("releaseDate") && issues.getJSONObject(i% 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).has("name")) { //se la lista non è vuota
                    injectedVersion = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).get("name").toString();
                    injectedVersionDate = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).get("releaseDate").toString();
                    dataFormattata2Injected = new DataManipulation().DataManipulationFormat(injectedVersionDate);
                }
                avList = new ArrayList<>();
                if(listAV.isEmpty())
                        injectedVersion = "N/A";

                //estraggo il nome della versione in cui il bug è stato risolto e la sua data di rilascio
                key = issues.getJSONObject(i%1000).get("key").toString(); // In general, the toString method returns a string that "textually represents" this object. The result should be a concise but informative representation that is easy for a person to read.System.out.println(key);

               if(!issues.isEmpty() && !issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").isEmpty()) { //se la lista non è vuota

                   fixVersion = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("name").toString();
                    String fixVersionDate = issues.getJSONObject(i % 1000).getJSONObject("fields").getString("resolutiondate");
                   fvDate = new DataManipulation().convertStringToDate(fixVersionDate);
                }
             //estraggo la data di apertura del bug
             String openingDate = issues.getJSONObject(i%1000).getJSONObject("fields").get("created").toString();
             String openingVersionDateFormatted = new DataManipulation().DataManipulationFormat(openingDate);
             Date ovDate = new DataManipulation().convertStringToDate(openingVersionDateFormatted);
             Date ivDate = dataFormattata2Injected == null ? null : new DataManipulation().convertStringToDate(dataFormattata2Injected);


             //considero solo i bug che hanno una versione affetta precedente alla data di apertura del bug
             if (ivDate!= null && ivDate.before(ovDate)) { //se la data di rilascio della versione affetta (esiste) è precedente alla data di apertura del bug e
                    if( fixVersion!=null && ovDate.before(fvDate) ) {
                        Release fv = new Release(fixVersion);
                        Release iv = new Release(injectedVersion, ivDate);

                        ManageRelease mr = new ManageRelease();
                        List<Release> releases = mr.retrieveReleases(projName);
                        for (Release release : releases) {
                            //assign the opening release the release such that the opening date is between the release created date and the next release created date
                            if (release.getDate().before(ovDate)) { //se la data di rilascio della release è precedente alla data di apertura del bug
                                    openingRelease = release; //la release di apertura è l'ultima della lista
                            }
                            if(release.getDate().after(fvDate)){
                                fvDate = release.getDate();
                                int fvIndex = mr.getReleaseIndex(releases, fvDate);
                                fv.setId(fvIndex);
                                fv.setDate(fvDate);
                            }
                            Release ov = new Release(this.openingRelease.getReleaseName(),this.openingRelease.getDate() );
                            int ovIndex = mr.getReleaseIndex(releases, openingRelease.getDate());
                            ov.setId(ovIndex);
                            if(iv.getReleaseName() != null) {
                                if (iv.getReleaseName().equals("N/A"))
                                    iv.setId(0);
                                else {
                                    ivIndex = mr.getReleaseIndex(releases, ivDate);
                                    iv.setId(ivIndex);
                                }

                                ticket = new IssueTicket(key, iv, fv, ov, avList);
                            }
                        }
                        

                       
                        tickets.add(ticket); //aggiungo il ticket alla lista dei ticket
                    }
             }
         }
      } while (i < total);
        return tickets;
    }

         //retrieve only consistent tickets

    public List<IssueTicket> retrieveConsistentTickets(List<IssueTicket> allTickets) throws IOException, ParseException {
                List<IssueTicket> consistentTickets = new ArrayList<>();

                for (IssueTicket ticket : allTickets) {
                    if (!ticket.injectedVersion.getReleaseName().equals("N/A") && ticket.openingVersion.getId() <= ticket.fixVersion.getId()) {
                        consistentTickets.add(ticket);
                    }
                }
                return consistentTickets;
    }

}
