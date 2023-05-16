package Utils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;

public class Csv2Arff {
    public static void convertCsv2Arff() throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.csv"));
        Instances data = loader.getDataSet();//get instances object

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.arff"));
        saver.writeBatch();
    }
}
