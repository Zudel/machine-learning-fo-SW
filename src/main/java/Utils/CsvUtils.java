package Utils;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import Entity.FileTouched;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;



public  class CsvUtils {


    public static void writeOnCsv(List<FileTouched> javaClassesList,String projectName) throws IOException {
        String filePath = "C:\\Users\\Roberto\\Documents\\GitHub\\deliverable-ISW2\\"+projectName+"-results_M1.csv";
        FileWriter fileWriter = new FileWriter(filePath);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = {"Release", "FileName", "LOC", "NR", "NAuth", "CC", "NComm","LocAdded", "MaxLocAdded", "AvgLocAdded","Churn", "MaxChurn", "AvgChurn", "Buggy"};
        csvWriter.writeNext(header);
        for (FileTouched fileTouched : javaClassesList){
            //write the data to csv file line by line
            String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()),  String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getChurn()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
            csvWriter.writeNext(data);
        }
        csvWriter.close();
    }

    public static void writeOnCsvStrings(List<String[]> results, String s) throws IOException {
        FileWriter fileWriter = new FileWriter(s);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String [] header = {"Dataset", "#TrainingRelease","Classifier","TP","FP","TN","FN","Precision", "Recall", "kappa", "AUC"};
        csvWriter.writeNext(header);
        for (String[] result : results){
            csvWriter.writeNext(result);
        }
        csvWriter.close();
    }
    public static void writeCsvRelease(List<FileTouched> fileToucheds, String path, int i, boolean training) throws IOException {
        FileWriter fileWriter = new FileWriter(path); //path of the csv file
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        String[] header = {"Release", "FileName", "LOC", "NR", "NAuth", "CC", "NComm","LocAdded", "MaxLocAdded", "AvgLocAdded","Churn", "MaxChurn", "AvgChurn", "Buggy{No,Yes}"};
        csvWriter.writeNext(header);
        if(!training) {
            for (FileTouched fileTouched : fileToucheds) {
                //write the data to csv file line by line
                if (fileTouched.getReleaseIndex() == i) {
                    String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()), String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getChurn()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
                    csvWriter.writeNext(data);
                }
            }
        }
        else {
            for (FileTouched fileTouched : fileToucheds) {
                //write the data to csv file line by line
                if (fileTouched.getReleaseIndex() < i) {
                    String[] data = {String.valueOf(fileTouched.getReleaseIndex()), fileTouched.getPathname(), String.valueOf(fileTouched.getSize()), String.valueOf(fileTouched.getNRev()), String.valueOf(fileTouched.getNAuth()), String.valueOf(fileTouched.getCc()), String.valueOf(fileTouched.getLocm()), String.valueOf(fileTouched.getLocAdded()), String.valueOf(fileTouched.getMaxLocAdded()), String.valueOf(fileTouched.getAvgLocAdded()), String.valueOf(fileTouched.getChurn()), String.valueOf(fileTouched.getMaxChurn()), String.valueOf(fileTouched.getAvgChurn()), String.valueOf(fileTouched.isBuggy())};
                    csvWriter.writeNext(data);
                }
            }
        }
        csvWriter.close();
    }

    public class CsvReader {
        public static List<FileTouched> leggiCsv(String path, String name) throws IOException {
            List<FileTouched> fileTouchedList = new ArrayList<>();

            try (CSVReader reader = new CSVReader(new FileReader(path + name))) {
                String[] lineInArray;
                while ((lineInArray = reader.readNext()) != null) {
                    if(lineInArray[0].equals("Release")) //skip header
                        continue;
                    int releaseId = Integer.parseInt(lineInArray[0]);
                    FileTouched fileTouched = new FileTouched(lineInArray[1], releaseId);
                    fileTouched.setSize(Integer.parseInt(lineInArray[2]));
                    fileTouched.setNRev(Integer.parseInt(lineInArray[3]));
                    fileTouched.setNAuth(Integer.parseInt(lineInArray[4]));
                    fileTouched.setCc(Integer.parseInt(lineInArray[5]));
                    fileTouched.setLocm(Integer.parseInt(lineInArray[6]));
                    fileTouched.setLocAdded(Integer.parseInt(lineInArray[7]));
                    fileTouched.setMaxLocAdded(Integer.parseInt(lineInArray[8]));
                    fileTouched.setAvgLocAdded(Double.parseDouble(lineInArray[9]));
                    fileTouched.setChurn(Integer.parseInt(lineInArray[10]));
                    fileTouched.setMaxChurn(Integer.parseInt(lineInArray[11]));
                    fileTouched.setAvgChurn(Double.parseDouble(lineInArray[12]));
                    fileTouched.setBuggy(Boolean.parseBoolean(lineInArray[13]));
                    fileTouchedList.add(fileTouched);
                }
            }
            return fileTouchedList;
        }
    }
}







