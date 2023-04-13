package org.example.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
/**
 * versions: data di rilascio delle versioni affette dal bug
 * fixVersions: data di rilascio delle versioni in cui il bug è stato risolto
 * */
public class RetrieveTicketsID {
    private static String fields ="fields";
    private static String rel= "released";
    private static String relDate="releaseDate";
    private static String projName ="BOOKKEEPER";
    private static final String FIX="fixVersions";
    private static final String FIELDS="fields";
    private static final String RELEASEDATE="releaseDate";
    private static final String VERSION="versions";
    private static final String DATAPATH="yyyy-MM-dd";

   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }

   public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONArray json = new JSONArray(jsonText);
         return json;
       } finally {
         is.close();
       }
   }

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try {
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
         String jsonText = readAll(rd);
         JSONObject json = new JSONObject(jsonText); //converto la stringa in un oggetto JSON
         return json;
       } finally {
         is.close();
       }
   }

  	   public static void main(String[] args) throws IOException, JSONException, ParseException {
       String affectedVersion;
       int count = 0;
	   Integer j, i = 0, total = 1;

      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;

         String url2 = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                  + projName + "%22AND%22issueType%22=%22Bug%22AND%20fixVersion%20is%20not%20EMPTY%20AND(%22status%22=%22closed%22OR"
                  + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,created,fixVersions,versions&created&startAt="
                  + i.toString() + "&maxResults=" + j.toString();
          JSONObject json2 = readJsonFromUrl(url2);
          //System.out.println(json2.toString());
          JSONArray issues = json2.getJSONArray("issues"); //Get the JSONArray value associated with a issues.

          total = json2.getInt("total");

         for (; i < total && i < j; i++) {
                count++;
                String key = issues.getJSONObject(i%1000).get("key").toString(); // In general, the toString method returns a string that "textually represents" this object. The result should be a concise but informative representation that is easy for a person to read.System.out.println(key);
             String fixVersionDate = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("releaseDate").toString();
                String fixVersionName = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("fixVersions").getJSONObject(0).get("name").toString();
                String affectedVersionDate = issues.getJSONObject(i%1000).getJSONObject("fields").get("created").toString();
                String dataFormattataResolved = new DataManipulation().DataManipulationFormat(fixVersionDate);
                String dataFormattata2Created = new DataManipulation().DataManipulationFormat(affectedVersionDate);
                System.out.println("Bug " + key + " - " + "Resolved:"+ dataFormattataResolved+ "   " + "Created:"+ dataFormattata2Created);
         }
      } while (i < total);
      System.out.println(count);
   }

 
}
