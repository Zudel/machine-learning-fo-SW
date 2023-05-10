package Utils;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import Entity.FileTouched;
import Entity.Release;
import com.opencsv.CSVWriter;

public class CsvWriter{
    private List<FileTouched> javaClassesList;

    public CsvWriter(List<FileTouched> javaClassesList) {
        this.javaClassesList = javaClassesList;
    }

    public void WriteOnCsv() throws IOException {
        String filePath = "C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.csv";
        FileWriter fileWriter = new FileWriter(filePath);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = {"Release", "File Name", "LOC", "NR", "NAuth", "CC", "NComm","LocAdded", "MaxLocAdded", "AvgLocAdded", "MaxChurn", "AvgChurn", "Buggy"};
        csvWriter.writeNext(header);
        for (FileTouched fileTouched : javaClassesList){
            //write the data to csv file line by line
            String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()),  String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
            csvWriter.writeNext(data);
        }
        csvWriter.close();
    }
}







