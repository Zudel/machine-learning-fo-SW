package Utils;

import Entity.FileTouched;
import Entity.Release;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ArffCreator {
    private File file;
    private String title;
    private String[] header = new String[]{"LOC","NR ","NAuth ",
            "CC","NComm","LOC_Added ","MAX_LOC_Added"," AVG_LOC_Added",
            "Churn ","MAX_Churn","AVG_Churn ","Buggy"};

    public ArffCreator(String filePath, String title) {
        this.file = new File(filePath);
        this.title = title;
    }
    public void writeData(Release release,List<FileTouched> fileTouchedList, boolean training) throws IOException {
        FileWriter fileWriter = new FileWriter(this.file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        //Titolo
        printWriter.println("@relation "+this.title);
        //Header
        for(String attribute: this.header){
            printWriter.println("@attribute "+attribute);
        }
        // Dati
        printWriter.println("@data");
        if (training){
            for (int i =1; i < release.getId(); i++) { //devo mettere la lista delle classi, che non stanno nella release!
                for (FileTouched javaClass : fileTouchedList) {
                    if (javaClass.getReleaseIndex() >= i)
                        continue;
                        String data = javaClass.getSize() + "," +
                        javaClass.getNRev() + "," +
                        javaClass.getNAuth() + "," +
                        javaClass.getCc() + "," +
                        javaClass.getLocm() + "," +
                        javaClass.getLocAdded() + "," +
                        javaClass.getMaxLocAdded() + "," +
                        javaClass.getAvgLocAdded() + "," +
                        javaClass.getChurn() + "," +
                        javaClass.getMaxChurn() + "," +
                        javaClass.getAvgChurn() +
                        "," + javaClass.isBuggy();
                        printWriter.println(data);
                }
            }
        }

        else {
            for (FileTouched javaClass : fileTouchedList) {
                if (javaClass.getReleaseIndex() == release.getId()) {
                    String data = javaClass.getSize() + "," +
                            javaClass.getNRev() + "," +
                            javaClass.getNAuth() + "," +
                            javaClass.getCc() + "," +
                            javaClass.getLocm() + "," +
                            javaClass.getLocAdded() + "," +
                            javaClass.getMaxLocAdded() + "," +
                            javaClass.getAvgLocAdded() + "," +
                            javaClass.getChurn() + "," +
                            javaClass.getMaxChurn() + "," +
                            javaClass.getAvgChurn() +
                            "," + javaClass.isBuggy();
                    printWriter.println(data);
                }
            }
        }

        printWriter.close();
    }

}
