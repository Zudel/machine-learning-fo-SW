package control;
import entity.FileTouched;
import entity.Release;
import utils.*;
import org.eclipse.jgit.revwalk.RevCommit;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.Csv2Arff.convertCsv2Arff;
import static utils.CsvUtils.writeCsvRelease;

public class Weka {
    private String pathM1;
    private  RetrieveProject project;
    private  List<Release> halfReleases;
    private  List<FileTouched> javaClasses;
    private static final String EXTENSION = ".arff";
    private boolean featureSelection;
    private SamplingType sampling;
    private CostSensitivityType costSensitivity;
    private String trainingFilePath;
    private String testingFilePath;
    private static final Logger logger = Logger.getLogger(Weka.class.getName());

    public Weka(RetrieveProject project, List<Release> halfReleases, List<FileTouched> javaClasses, boolean featureSelection, SamplingType sampling, CostSensitivityType costSensitivity) {
        this.costSensitivity = costSensitivity;
        this.sampling = sampling;
        this.featureSelection = featureSelection;
        this.project = project;
        this.halfReleases = halfReleases;
        this.javaClasses = javaClasses;
        this.pathM1 =  "C:\\Users\\Roberto\\Documents\\GitHub\\"+project.getProjName()+"-results_M1.csv";
    }
        public  void wekaWork() throws Exception {
            //retrieve the data from Milestone1.arff
            // Carica i dati dal file ARFF
            List<String[]> results = new ArrayList<>();
            Evaluation eval;
            convertCsv2Arff(pathM1); //converte il file csv in arff e lo salva in C:\Users\Roberto\Documents\GitHub\Milestone1.arff
            Instances data = new Instances(new FileReader( "C:\\Users\\Roberto\\Documents\\GitHub\\"+project.getProjName()+"-results_M1.arff"));

            data.setClassIndex(data.numAttributes() - 1);
            Classifier[] classifiers = new Classifier[] {
                    new IBk(), // Albero decisionale
                    new NaiveBayes(), // Naive Bayes
                    new RandomForest() // Foresta casuale
            };

                for (int i = 1; i <= halfReleases.size(); i++) {
                     trainingFilePath =  "C:\\Users\\Roberto\\Documents\\GitHub\\"+project.getProjName()+"-trainingRelease_"+(i);
                     testingFilePath =  "C:\\Users\\Roberto\\Documents\\GitHub\\"+project.getProjName()+"-testingRelease_"+(i);
                     List<FileTouched> classesRelabeled = relabeling(javaClasses, halfReleases.get(i-1));
                     writeCsvRelease(classesRelabeled, trainingFilePath+".csv", halfReleases.get(i-1).getId(), true);
                     writeCsvRelease(javaClasses, testingFilePath+".csv", halfReleases.get(i-1).getId(), false);

                    convertCsv2Arff(trainingFilePath+".csv"); //converte il file csv in arff e lo salva in C:\Users\Roberto\Documents\GitHub\Milestone1.arff
                    convertCsv2Arff(testingFilePath+".csv"); //converte il file csv in arff e lo salva in C:\Users\Roberto\Documents\GitHub\Milestone1.arff
                    int j=0;
                    for (Classifier classifier : classifiers) {
                            String[] result = new String[11];
                            String nameClassifier = classifiers[j].getClass().getSimpleName();
                            DataSource source1 = new DataSource(trainingFilePath + EXTENSION);
                            Instances training = source1.getDataSet(); //java.io.IOException: Unable to determine structure as arff (Reason: java.io.IOException: premature end of line, read Token[EOL], line 3).
                            DataSource source2 = new DataSource(testingFilePath + EXTENSION);
                            Instances testing = source2.getDataSet(); //java.io.IOException: Unable to determine structure as arff (Reason: java.io.IOException: premature end of line, read Token[EOL], line 3).
                        try {


                            if (featureSelection) {
                                //create AttributeSelection object
                                AttributeSelection filter = new AttributeSelection();
                                //create evaluator and search algorithm objects
                                CfsSubsetEval subsetEval = new CfsSubsetEval();
                                GreedyStepwise search = new GreedyStepwise();
                                //set the algorithm to search backward
                                search.setSearchBackwards(true);
                                filter.setEvaluator(subsetEval);
                                filter.setSearch(search);
                                //specify the dataset
                                filter.setInputFormat(training);
                                testing = Filter.useFilter(testing, filter);
                                training = Filter.useFilter(training, filter);
                            }
                            int numAttr = training.numAttributes();
                            training.setClassIndex(numAttr - 1); //setta l'indice della classe per l'istanza corrente
                            testing.setClassIndex(numAttr - 1); //setta l'indice della classe per l'istanza corrente
                            classifier.buildClassifier(training); //costruisce il classificatore
                            if(sampling != null)
                                classifier = setSampling(sampling,classifier, training); //setta il campionamento
                            if (costSensitivity != null) {
                                classifier = setCostSensitive( classifier, training); //setta la sensibilità al costo
                            }
                                eval = new Evaluation(testing); //crea un oggetto Evaluation per valutare il classificatore sul testing set
                                eval.evaluateModel(classifier, testing); //valuta il classificatore sul testing set e restituisce un oggetto Evaluation contenente i risultati
                            }
                            catch (Exception e) { //IBk: cannot handle unary class!
                                logger.log(Level.OFF, e.getMessage());
                                continue; //passa alla prossima release
                            }


                            result[0] = project.getProjName();
                            result[1] = String.valueOf(i-1);
                            result[2] = nameClassifier; //nome del classificatore utilizzato
                            result[3] = String.valueOf(eval.numTruePositives(0));
                            result[4] = String.valueOf(eval.numFalsePositives(0));
                            result[5] = String.valueOf(eval.numTrueNegatives(0));
                            result[6] = String.valueOf(eval.numFalseNegatives(0));
                            result[7] = String.valueOf(eval.precision(0));
                            result[8] = String.valueOf(eval.recall(0));
                            result[9] = String.valueOf(eval.kappa());
                            result[10] = String.valueOf(eval.areaUnderROC(0));
                            results.add(result);
                            j++;
                            CsvUtils.writeOnCsvStrings(results, project.getProjName() + "-results_M2" + ".csv");
                        }
                }
    }

    private List<FileTouched> relabeling(List<FileTouched> javaClasses, Release release)  {
        List<FileTouched> list = new ArrayList<>();
        for (FileTouched javaClass : javaClasses) {
            FileTouched fileTouched = javaClass;
            int commits = javaClass.getCommits().size();
            for (int i = 0; i < commits; i++) {
                RevCommit commit = javaClass.getCommits().get(i);
                if (commit.getAuthorIdent().getWhen().after(release.getDate()) && javaClass.isBuggy()) {
                    fileTouched.setBuggy(false);//setta la classe come non buggy se è stata modificata dopo la training release
                    javaClass.setBuggy(false);
                    break;
                }
             }
            list.add(fileTouched);
        }
        return list;
    }

    private Classifier setSampling(SamplingType sampling, Classifier classifier, Instances training) throws Exception {

            FilteredClassifier fc = new FilteredClassifier(); //crea un classificatore filtrato
            fc.setClassifier(classifier); //setta il classificatore
            Filter filter = null;
            switch (sampling) {
                case UNDERSAMPLING -> {
                    filter = new SpreadSubsample(); //crea un filtro di tipo SpreadSubsample
                    String[] opts = new String[]{"-M", "1.0"};
                    filter.setOptions(opts); //
                }
                case OVERSAMPLING -> {
                    filter = new Resample();
                    filter.setOptions(new String[]{"-B", "1.0", "-Z", "130.3"});
                    filter.setInputFormat(training);
                }
                /**
                 "-C", "0": Imposta il valore dell'opzione "class index" a 0. Questo indica che l'etichetta di classe è posizionata nella prima colonna dei dati.
                 "-K", "5": Imposta il numero di vicini da utilizzare nell'algoritmo SMOTE a 5. Questo determina quanti campioni sintetici verranno generati per ogni campione di classe minoritaria.
                 "-P", "100.0": Imposta il livello di sovracampionamento (over-sampling) desiderato al 100%. Questo significa che il numero di campioni di classe minoritaria nel set di dati verrà raddoppiato.
                 "-S", "1": Imposta il seme random per l'algoritmo SMOTE a 1. Questo garantisce la riproducibilità dei risultati, poiché lo stesso seme genererà gli stessi campioni sintetici ogni volta che viene eseguito il filtro SMOTE.*/

                case SMOTE -> { //Synthetic Minority Over-sampling Technique per il bilanciamento delle classi.
                    filter = new SMOTE();
                    filter.setOptions(new String[]{"-C", "0", "-K", "5", "-P", "100.0", "-S", "1"});
                    filter.setInputFormat(training); //setta il formato di input del filtro con il training set
                }
            }
            fc.setFilter(filter);
            fc.buildClassifier(training);
            classifier = fc;
        return classifier;
    }

    private Classifier setCostSensitive(Classifier classifier, Instances training) throws Exception {
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier(); //crea un classificatore sensibile al costo (cost-sensitive)
        CostMatrix matrix = new CostMatrix(2); //crea una matrice dei costi 2x2
        if (costSensitivity == CostSensitivityType.SENSITIVITY_LEARNING) { //se il tipo di sensibilità al costo è SENSITIVITY_LEARNING
            matrix.setCell(0, 1, 1.0); //setta il valore della cella (0,1) a 1.0 (costo di classificazione di un'istanza negativa come positiva)
            matrix.setCell(1, 0, 10.0); //setta il valore della cella (1,0) a 10.0 (costo di classificazione di un'istanza positiva come negativa)
            costSensitiveClassifier.setCostMatrix(matrix); //setta la matrice dei costi del classificatore
            costSensitiveClassifier.setMinimizeExpectedCost(false); //setta il valore di minimizeExpectedCost a false (non minimizzare il costo atteso)
        } else { //se il tipo di sensibilità al costo è COST_THRESHOLD
            matrix.setCell(0, 1, 1.0);
            matrix.setCell(1, 0, 1.0);
            costSensitiveClassifier.setCostMatrix(matrix);
            costSensitiveClassifier.setMinimizeExpectedCost(true); //setta il valore di minimizeExpectedCost a true (minimizzare il costo atteso)
        }
        costSensitiveClassifier.setClassifier(classifier);
        costSensitiveClassifier.buildClassifier(training);
        return costSensitiveClassifier;
    }
}


