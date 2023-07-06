package utils;

public class RetrieveProject {
        private String project ;
        private String projectDir;
        private String deliverableProjectPath = "C:\\Users\\Roberto\\Documents\\GitHub";
        public String getProjName() {
            return project;
        }
        public String getProjDirName() {
            return projectDir;
        }
        public String getDeliverableProjectPath() {
            return deliverableProjectPath;
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
