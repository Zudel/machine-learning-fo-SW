package utils;

import entity.IssueTicket;
import entity.Release;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static utils.JSONUtils.readJsonFromUrl;
import static utils.ManageRelease.retrieveReleases;

/**
 * versions: data di rilascio delle versioni affette dal bug
 * fixVersions: data di rilascio delle versioni in cui il bug è stato risolto
 * */
public class RetrieveJiraTickets {
    private static String rel= "released";
    private String dataFormattata2Injected;
    private String key;
    private JSONObject json2;
    private String fixVersion;
    private int ivIndex;
    private Release ov;
    private String RELEASEDATE = "releaseDate";
    private String RELEASEVERSION = "versions";


    public List<IssueTicket> retrieveTickets(String projName) throws IOException, ParseException {
       String injectedVersion=null;
       String injectedVersionDate;
       Integer j, i = 0, total;
        Date fvDate;
       List<IssueTicket> tickets = new ArrayList<>();
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
           for (; i < total && i < j; i++) {

               //estraggo tutte le informazioni che mi servono dal JSON dei report e le salvo in variabili locali
               key = issues.getJSONObject(i % 1000).get("key").toString(); //Get the JSONObject value associated with a key.
               JSONObject fields = issues.getJSONObject(i % 1000).getJSONObject("fields"); //Get the JSONObject value associated with a fields.
               JSONArray listAV = fields.getJSONArray(RELEASEVERSION); //Get the JSONArray value associated with a versions affected.

               if (!listAV.isEmpty() && issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray(RELEASEVERSION).getJSONObject(0).has(RELEASEDATE) && issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions").getJSONObject(0).has("name")) { //se la lista non è vuota
                   injectedVersion = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray(RELEASEVERSION).getJSONObject(0).get("name").toString();
                   injectedVersionDate = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray(RELEASEVERSION).getJSONObject(0).get(RELEASEDATE).toString();
                   dataFormattata2Injected = new DataManipulation().dataManipulationFormat(injectedVersionDate);
               }
               if (listAV.isEmpty())
                   injectedVersion = "N/A";

               Date ivDate = dataFormattata2Injected == null ? null : new DataManipulation().convertStringToDate(dataFormattata2Injected);
               if (!issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("fixVersions").isEmpty()) {
                   fixVersion = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("name").toString();
                   String fixVersionDate = issues.getJSONObject(i % 1000).getJSONObject("fields").getString("resolutiondate");
                   fvDate = new DataManipulation().convertStringToDate(fixVersionDate);
               } else {
                   fixVersion = "N/A";
                   fvDate = null;
               }
               //estraggo la data di apertura del bug

               String openingDate = issues.getJSONObject(i % 1000).getJSONObject("fields").get("created").toString();
               String openingVersionDateFormatted = new DataManipulation().dataManipulationFormat(openingDate);
               Date ovDate = new DataManipulation().convertStringToDate(openingVersionDateFormatted);

               ManageRelease mr = new ManageRelease();
               List<Release> releases = retrieveReleases(projName);
               ov = getRelease(releases, ovDate);

                   //considero solo i bug che hanno una versione affetta precedente alla data di apertura del bug
                   if (ov!= null && ovDate != null && fvDate!= null  && fvDate.after(ovDate)) { //se la data di rilascio della versione affetta (esiste) è precedente alla data di apertura del bug e

                            int fvIndex = mr.getReleaseIndexByName(releases, fixVersion); //prendo l'indice della release
                           Release fv = new Release(fixVersion, fvDate, fvIndex); //creo la release di chiusura
                           int ovIndex = mr.getReleaseIndexByDate(releases, ov.getDate());
                           ov.setId(ovIndex);
                           Release iv = new Release(injectedVersion, ivDate); //creo la release di apertura
                           if (iv.getReleaseName() != null) { //se la versione affetta esiste e la data di chiusura del bug esiste
                               if (iv.getReleaseName().equals("N/A"))
                                   iv.setId(0);
                               else {
                                   ivIndex = mr.getReleaseIndexByDate(releases, iv.getDate());
                                   iv.setId(ivIndex);
                               }
                               if(ov.getId() == -1)
                                   ov.setId(fvIndex);
                               if (ov.getId() <= fv.getId() && iv.getId() < fv.getId() && !(injectedVersion.equals(fixVersion))){
                                    IssueTicket ticket = new IssueTicket(key, iv, fv, ov);
                                   tickets.add(ticket); //aggiungo il ticket alla lista dei ticket
                               }
                           }
                   }
           }
      } while (i < total);
        return tickets;
    }

    private Release getRelease(List<Release> releases, Date date) {
        for (Release release : releases) {
            if (release.getDate().after(date)) {
                return release;
            }
        }
        return null;
    }
    //retrieve only consistent tickets

    public List<IssueTicket> retrieveConsistentTickets(List<IssueTicket> allTickets) {
                List<IssueTicket> consistentTickets = new ArrayList<>();

                for (IssueTicket ticket : allTickets) {
                    if (ticket != null && !ticket.getInjectedVersion().getReleaseName().equals("N/A")) {
                        consistentTickets.add(ticket);
                    }
                }
                return consistentTickets;
    }
}
