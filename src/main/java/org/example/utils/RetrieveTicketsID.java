package org.example.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RetrieveTicketsID {
    private static String fields ="fields";
    private static String rel= "released";
    private static String relDate="releaseDate";
    private List<InfoVersion> listVersion;
    private static String projName ="AVRO";
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

    public List<InfoVersion> listVersion() throws IOException, JSONException, ParseException {
        Integer i = 0;
        Integer total ;
        listVersion = new ArrayList<>();
        String url= "https://issues.apache.org/jira/rest/api/2/project/"+this.projName+"/version?";
        JSONObject json = readJsonFromUrl(url);
        JSONArray issues = json.getJSONArray("values");
        total = json.getInt("total");
        for (; i < total ; i++){
            String name=issues.getJSONObject(i).get("name").toString();
            Date day=null;
            if(!issues.getJSONObject(i).isNull(RELEASEDATE)) {
                day = new SimpleDateFormat(DATAPATH).parse(issues.getJSONObject(i).get(RELEASEDATE).toString());
                InfoVersion f = new InfoVersion(day, name);
                listVersion.add(f);
            }
        }
        //sort(listVersion);
        return listVersion;
    }

  	   public static void main(String[] args) throws IOException, JSONException, ParseException {
		   

	   Integer j, i = 0, total = 1;
       //String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
       //            + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"+"%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions";
       //JSONObject json = new JSONObject(readJsonFromUrl(url));
       /*Iterator<String> keys = json.keys();
       while(keys.hasNext()) {
           String key = keys.next();
               System.out.println("Nome attributo: " + key); // crea un iteratore keys che contiene tutti i nomi degli attributi presenti nell'oggetto JSON.
           }*/

      //Get JSON API for closed bugs w/ AV in the project
      do {
         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
         j = i + 1000;
         /*String url2 = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();*/
          String url2 = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                  + projName + "%22AND%22issueType%22=%22Bug%22AND%20fixVersion%20is%20not%20EMPTY%20AND(%22status%22=%22closed%22OR"
                  + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,created,fixVersions,versions&created&startAt="
                  + 0 + "&maxResults=" + 1000;
         JSONObject json2 = readJsonFromUrl(url2);
         JSONArray issues = json2.getJSONArray("issues"); //Get the JSONArray value associated with a issues.
          //System.out.println(issues.getJSONObject(0).get(relDate));
          total = json2.getInt("total");

         for (; i < total && i < j; i++) {
            //Iterate through each bug
             //System.out.println(issues.getJSONObject(i%1000));
            String key = issues.getJSONObject(i%1000).get("key").toString(); // In general, the toString method returns a string that "textually represents" this object. The result should be a concise but informative representation that is easy for a person to read.System.out.println(key);
             JSONArray fixVer = issues.getJSONObject(i%1000).getJSONObject(fields).getJSONArray("fixVersions");
             Version fv = new Version();
             if(!fixVer.isEmpty()) {
                 fv.setName(fixVer.getJSONObject(0).get("name").toString());
                 //if (fixVer.getJSONObject(0).get(rel).toString().equals("true"))
                    // fv.setReleaseDate(fixVer.getJSONObject(0).get(RELEASEDATE).toString());
                 iterate(fixVer, fv);
             }
             JSONArray affVer = issues.getJSONObject(i%1000).getJSONObject(fields).getJSONArray("versions");
             Version av = new Version();
             if(!affVer.isEmpty()) {
                 av.setName(affVer.getJSONObject(0).get("name").toString());
                 //if(affVer.getJSONObject(0).get(rel).toString().equals("true")){

                     //av.setReleaseDate(affVer.getJSONObject(0).get(RELEASEDATE).toString()); }
                     iterate(affVer, av);
             }
             System.out.println(key+"  " + "affected_version: "+ affVer.toString());
             System.out.println(key +"  "+ "fixed_version: " + fixVer.toString());


             //String summary = issues.getJSONObject(i%1000).getJSONObject("fields").getJSONArray("versions").toString();
                // String summary = issues.getJSONObject(i%1000).getJSONObject("fields").get("resolutiondate").toString();
                 //String summary2 = issues.getJSONObject(i%1000).getJSONObject("fields").get("created").toString();

                 //String dataFormattata = new DataManipulation().DataManipulationFormat(summary);
                 //String dataFormattata2 = new DataManipulation().DataManipulationFormat(summary2);
                 //System.out.println("Bug " + key + " - " + "Resolved:"+ dataFormattata+"   " + "Created:"+ dataFormattata2);
         }
      } while (i < total);
   }
    private static void  iterate(JSONArray ver, Version v) throws ParseException {
        for(int k=1;k<ver.length();k++){
            if(ver.getJSONObject(k).get(rel).toString().equals("true") && v.getReleaseDate()!=null && v.getReleaseDate().before(new SimpleDateFormat("yyyy-MM-dd").parse(ver.getJSONObject(k).get(relDate).toString()))){
                v.setReleaseDate(ver.getJSONObject(k).get(relDate).toString());
                v.setName(ver.getJSONObject(k).get("name").toString());
            }
        }
    }

 
}
