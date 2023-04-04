package org.example.utils;

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
