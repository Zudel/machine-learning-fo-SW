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
    private Release openingRelease;


    public List<IssueTicket> retrieveTickets(String projName) throws IOException, ParseException {
       String injectedVersion = null;
       String injectedVersionDate;
       int count = 0;
       int countAV = 0;
        Integer j, i = 0, total = 1;
       List<IssueTicket> tickets = new ArrayList<>();
       List<Release> avList = new ArrayList<>();
       Map<Date, String> unsortedReleasesMap = new HashMap<>();    //Date = release date; String = release name (e.g. 1.4.2

      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;

         String url2 = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                  + projName + "%22AND%22issueType%22=%22Bug%22AND%20fixVersion%20is%20not%20EMPTY%20AND(%22status%22=%22closed%22OR"
                  + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,created,fixVersions,versions&created&startAt="
                  + i.toString() + "&maxResults=" + j.toString();

          JSONObject json2 = readJsonFromUrl(url2);
          JSONArray issues = json2.getJSONArray("issues"); //Get the JSONArray value associated with a issues.

          total = json2.getInt("total");

           for (; i < total && i < j; i++) {

                //estraggo tutte le informazioni che mi servono dal JSON dei report e le salvo in variabili locali
               JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
               JSONArray listAV = fields.getJSONArray("versions");
                if(!listAV.isEmpty()) { //se la lista non è vuota
                    injectedVersion = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).get("name").toString();
                    injectedVersionDate = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).get("releaseDate").toString();
                    dataFormattata2Injected = new DataManipulation().DataManipulationFormat(injectedVersionDate);
                }
                    Release av= null;
                    avList = new ArrayList<>();
                    /*for(int z = 0; z < listAV.length(); z++){ //per ogni versione affetta dal bug estraggo il nome

                       /* av.setReleaseName(listAV.getJSONObject(z).get("name").toString());
                        if(!avList.contains(av) && !injectedVersion.equals("N/A")) //se la lista non contiene già il nome della versione
                            avList.add(av); //aggiungo alla lista delle versioni affette il nome della versione

                        }


                }

                else {  //se la lista è vuota
                    injectedVersion = "N/A";
                    avList.clear();
                }*/
                if(listAV.isEmpty())
                    injectedVersion = "N/A";
                //estraggo il nome della versione in cui il bug è stato risolto e la sua data di rilascio
                key = issues.getJSONObject(i%1000).get("key").toString(); // In general, the toString method returns a string that "textually represents" this object. The result should be a concise but informative representation that is easy for a person to read.System.out.println(key);
                String fixVersion=issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("name").toString();
                String fixVersionDate = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("releaseDate").toString();
                String dataFormattataResolved = new DataManipulation().DataManipulationFormat(fixVersionDate);

             //estraggo la data di apertura del bug
             String openingDate = issues.getJSONObject(i%1000).getJSONObject("fields").get("created").toString();
             String openingVersionDateFormatted = new DataManipulation().DataManipulationFormat(openingDate);

             Date ovDate = new DataManipulation().convertStringToDate(openingVersionDateFormatted);
             Date ivDate = dataFormattata2Injected == null ? null : new DataManipulation().convertStringToDate(dataFormattata2Injected);
             Date fvDate = new DataManipulation().convertStringToDate(dataFormattataResolved);

             //considero solo i bug che hanno una versione affetta precedente alla data di apertura del bug
             if (ivDate!= null && ivDate.before(ovDate)) { //se la data di rilascio della versione affetta (esiste) è precedente alla data di apertura del bug e
                    if(!fixVersion.equals(injectedVersion)) { //se la versione in cui il bug è stato risolto è diversa dalla versione affetta
                        Release fv = new Release(fixVersion, fvDate);
                        Release iv = new Release(injectedVersion, ivDate);

                        ManageRelease mr = new ManageRelease();
                        List<Release> releases = mr.retrieveReleases(projName);
                        for (Release release : releases) {
                            if (release.getDate().after(ovDate)) {
                                 openingRelease = release;
                                break;
                            }
                        }
                        Release ov = new Release(openingRelease.getReleaseName(),openingRelease.getDate() );
                        int ivIndex = mr.getReleaseIndex(releases, ivDate);
                        int fvIndex = mr.getReleaseIndex(releases, fvDate);
                        int ovIndex = mr.getReleaseIndex(releases, openingRelease.getDate());
                        if(iv.getReleaseName().equals("N/A"))
                            iv.setId(0);
                        else
                            iv.setId(ivIndex);
                        fv.setId(fvIndex);
                        ov.setId(ovIndex);
                        System.out.println("Bug: " + key + " - IV: " + iv.getReleaseName() + " - OV: " + ov.getReleaseName() + " - FV: " + fv.getReleaseName());
                        System.out.println("Bug: " + key + " - IV: " + iv.getId() + " - OV: " + ov.getId() + " - FV: " + fv.getId());

                        

                        IssueTicket ticket = new IssueTicket(key, iv, fv, ov, avList);
                        tickets.add(ticket); //aggiungo il ticket alla lista dei ticket
                    }
             }
         }
      } while (i < total);
       /* System.out.println("Total bugs: " + count);
        System.out.println("Total bugs with available AV: " + countAV);
        System.out.println("Total bugs with no avalaible AV: " + (count-countAV));*/
        return tickets;
         }

    /*This method retrieves all the versions of the project (Avro or Bookkeeper) that are released and with a release date*/



}
