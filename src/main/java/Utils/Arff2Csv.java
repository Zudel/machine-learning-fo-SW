package Utils;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVSaver;

import java.io.File;

public class Arff2Csv {
    public static void arff2Csv(String pathName) throws Exception {

        // load ARFF
        ArffLoader loader = new ArffLoader();
        loader.setSource(new File(pathName));
        Instances data = loader.getDataSet();//get instances object

        // save CSV
        CSVSaver saver = new CSVSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as CSV
        saver.setFile(new File(pathName));
        saver.writeBatch();
    }
}
