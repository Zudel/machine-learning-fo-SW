package utils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import java.io.File;

public class Csv2Arff {
    private Csv2Arff() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }
    public static void convertCsv2Arff(String pathName) throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(pathName));
        Instances data = loader.getDataSet();//get instances object

        String[] options = new String[]{"-R", "1,2"}; // remove 1st and 2nd attributes
        Remove removeFilter = new Remove();
        removeFilter.setOptions(options);
        removeFilter.setInputFormat(data);
        Instances newData = Filter.useFilter(data, removeFilter);

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File(pathName.replace(".csv", ".arff"))); //
        saver.writeBatch();
    }
}
