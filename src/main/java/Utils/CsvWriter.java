package Utils;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import Entity.FileTouched;
import com.opencsv.CSVWriter;

public  class CsvWriter{
    public static void writeOnCsv(List<FileTouched> javaClassesList) throws IOException {
        String filePath = "C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.csv";
        FileWriter fileWriter = new FileWriter(filePath);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = {"Release", "File Name", "LOC", "NR", "NAuth", "CC", "NComm","LocAdded", "MaxLocAdded", "AvgLocAdded","Churn", "MaxChurn", "AvgChurn", "Buggy"};
        csvWriter.writeNext(header);
        for (FileTouched fileTouched : javaClassesList){
            //write the data to csv file line by line
            String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()),  String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getChurn()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
            csvWriter.writeNext(data);
        }
        csvWriter.close();
    }

    public static void writeOnCsv(List<String[]> results, String s) throws IOException {
        FileWriter fileWriter = new FileWriter(s);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String [] header = {"Classifier", "TP", "FP", "TN", "FN", "Precision", "Recall", "kappa", "AUC"};
        csvWriter.writeNext(header);
        for (String[] result : results){
            csvWriter.writeNext(result);
        }
        csvWriter.close();

    }

    public void writeCsv(List<FileTouched> fileToucheds, String s) throws IOException {
        FileWriter fileWriter = new FileWriter(s);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = {"Release", "File Name", "LOC", "NR", "NAuth", "CC", "NComm","LocAdded", "MaxLocAdded", "AvgLocAdded","Churn", "MaxChurn", "AvgChurn", "Buggy"};
        csvWriter.writeNext(header);
        for (FileTouched fileTouched : fileToucheds){
            //write the data to csv file line by line
            String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()),  String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getChurn()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
            csvWriter.writeNext(data);
        }
        csvWriter.close();

    }
}







