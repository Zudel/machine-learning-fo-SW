package org.example.utils;

import org.apache.htrace.shaded.fasterxml.jackson.databind.JsonNode;
import org.apache.htrace.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;

public class RetrieveProject {
        private String project, projectDir;
        public String getProjName() {
            return project;
        }
        public String getProjDirName() {
            return projectDir;
        }
        public RetrieveProject(boolean bool){
            if(bool){
                this.project ="AVRO";
                this.projectDir = "C:\\Users\\Roberto\\Documents\\GitHub\\avro";
            }else{
                this.project ="BOOKKEEPER";
                this.projectDir = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";
            }
        }

}
