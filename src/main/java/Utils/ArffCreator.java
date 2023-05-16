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
            "Churn ","MAX_Churn","AVG_Churn ","Buggy {'true', 'false'}"};

    public ArffCreator(String filePath, String title) {
        this.file = new File(filePath);
        this.title = title;
    }
    public void writeData(List<Release> releases,List<FileTouched> fileTouchedList, boolean training) throws IOException {
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
        for (int i =0; i<= releases.size(); i++){ //devo mettere la lista delle classi, che non stanno nella release!
            for(FileTouched javaClass: fileTouchedList){
                if(javaClass.getReleaseIndex() != i)
                    continue;
                System.out.println("file: " + javaClass.getPathname() + " id: " + javaClass.getReleaseIndex() + " loc: " + javaClass.getSize() + " churn: " + javaClass.getChurn());

                String data = javaClass.getSize() +","+
                        javaClass.getNRev() +","+
                        javaClass.getNAuth() +","+
                        javaClass.getCc() +","+
                        javaClass.getLocm() +","+
                        javaClass.getLocAdded() +","+
                        javaClass.getMaxLocAdded() +","+
                        javaClass.getAvgLocAdded() +","+
                        javaClass.getChurn() +","+
                        javaClass.getMaxChurn() +","+
                        javaClass.getAvgChurn();
                if(training){
                    data += ","+javaClass.isBuggy();
                    printWriter.println(data);
                }
            }
        }

        printWriter.close();
    }

}
