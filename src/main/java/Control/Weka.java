package Control;
import Entity.FileTouched;
import Entity.IssueTicket;
import Entity.Release;
import Utils.*;
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
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static Utils.Csv2Arff.convertCsv2Arff;
import static Utils.SamplingType.SMOTE;

public class Weka {
    private static String pathCsv = "C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.csv";
    private static String pathArff = "C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.arff";
    private  RetrieveProject project;
    private  List<Release> halfReleases;
    private  List<FileTouched> javaClasses;
    private static final String EXTENSION = ".arff";
    private boolean featureSelection;
    private SamplingType sampling;
    private CostSensitivityType costSensitivity;
    public Weka(RetrieveProject project, List<Release> halfReleases, List<FileTouched> javaClasses, boolean featureSelection, SamplingType sampling, CostSensitivityType costSensitivity) {
        this.costSensitivity = costSensitivity;
        this.sampling = sampling;
        this.featureSelection = featureSelection;
        this.project = project;
        this.halfReleases = halfReleases;
        this.javaClasses = javaClasses;



    }
        public  void wekaWork() throws Exception {
            //retrieve the data from Milestone1.arff
            // Carica i dati dal file ARFF
            String trainingFile = "training-set-"+project.getProjName();
            String testingFile = "testing-set-"+project.getProjName();
            List<String[]> results = new ArrayList<>();
            convertCsv2Arff(); //converte il file csv in arff e lo salva in C:\Users\Roberto\Documents\GitHub\Milestone1.arff
            Instances data = new Instances( new FileReader("C:\\Users\\Roberto\\Documents\\GitHub\\Milestone1.arff"));
            data.setClassIndex(data.numAttributes() - 1);
            Classifier[] classifiers = new Classifier[] {
                    new IBk(), // Albero decisionale
                    new NaiveBayes(), // Naive Bayes
                    new RandomForest() // Foresta casuale
            };
            //cicla sul numero di istanze
            /*for (int i = 0; i < data.numInstances(); i++) {
                System.out.println(data.get(i).classValue()); //stampa il valore della classe per ogni istanza (0 o 1)
                // il metodo classValue restituisce il valore della classe per l'istanza corrente
            }*/
                for (int i = 1; i <= halfReleases.size(); i++) {
                    for (FileTouched fileTouched : javaClasses) {
                        if (fileTouched.getReleaseIndex() == i) {
                            System.out.println(fileTouched.getReleaseIndex() + " " + fileTouched.getPathname());
                        }
                    }
                        for (Classifier classifier : classifiers) {
                            ArffCreator trainingSet = new ArffCreator(trainingFile + EXTENSION, trainingFile); //crea il file arff per il training set
                            ArffCreator testingSet = new ArffCreator(testingFile + EXTENSION, testingFile); //crea il file arff per il testing set

                            String[] result = new String[8];
                            trainingSet.writeData(halfReleases.subList(0, i), javaClasses, true); //scrive i dati nel file arff per il training set
                            testingSet.writeData(List.of(halfReleases.get(i)),javaClasses, false); //scrive i dati nel file arff per il testing set

                            DataSource source1 = new DataSource(trainingFile + EXTENSION);
                            Instances training = source1.getDataSet(); //java.io.IOException: Unable to determine structure as arff (Reason: java.io.IOException: premature end of line, read Token[EOL], line 3).
                            DataSource source2 = new DataSource(testingFile + EXTENSION);
                            Instances testing = source2.getDataSet(); //java.io.IOException: Unable to determine structure as arff (Reason: java.io.IOException: premature end of line, read Token[EOL], line 3).

                            if (featureSelection) {
                                //create AttributeSelection object
                                AttributeSelection filter = new AttributeSelection();
                                //create evaluator and search algorithm objects
                                CfsSubsetEval eval = new CfsSubsetEval();
                                GreedyStepwise search = new GreedyStepwise();
                                //set the algorithm to search backward
                                search.setSearchBackwards(true);
                                filter.setEvaluator(eval);
                                filter.setSearch(search);
                                //specify the dataset
                                filter.setInputFormat(training);
                                training = Filter.useFilter(training, filter);
                                testing = Filter.useFilter(testing, filter);
                            }
                            int numAttr = training.numAttributes();
                            training.setClassIndex(numAttr - 1); //setta l'indice della classe per l'istanza corrente
                            testing.setClassIndex(numAttr - 1); //setta l'indice della classe per l'istanza corrente
                            classifier.buildClassifier(training); //costruisce il classificatore
                            if (sampling != null) {
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
                            }
                            if (costSensitivity != null) {
                                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
                                CostMatrix matrix = new CostMatrix(2);
                                if (costSensitivity == CostSensitivityType.SENSITIVITY_LEARNING) {
                                    matrix.setCell(0, 1, 1.0);
                                    matrix.setCell(1, 0, 10.0);
                                    costSensitiveClassifier.setCostMatrix(matrix);
                                    costSensitiveClassifier.setMinimizeExpectedCost(false);
                                } else {
                                    matrix.setCell(0, 1, 1.0);
                                    matrix.setCell(1, 0, 1.0);
                                    costSensitiveClassifier.setCostMatrix(matrix);
                                    costSensitiveClassifier.setMinimizeExpectedCost(true);
                                }

                                costSensitiveClassifier.setClassifier(classifier);
                                costSensitiveClassifier.buildClassifier(training);
                                classifier = costSensitiveClassifier;
                            }
                            Evaluation eval = new Evaluation(testing);
                            eval.evaluateModel(classifier, testing);

                            result[0] = String.valueOf(eval.numTruePositives(0));
                            result[1] = String.valueOf(eval.numFalsePositives(0));
                            result[2] = String.valueOf(eval.numTrueNegatives(0));
                            result[3] = String.valueOf(eval.numFalseNegatives(0));
                            result[4] = String.valueOf(eval.precision(0));
                            result[5] = String.valueOf(eval.recall(0));
                            result[6] = String.valueOf(eval.kappa());
                            result[7] = String.valueOf(eval.areaUnderROC(0));
                            results.add(result);
                        }
                        CsvWriter.writeOnCsv(results, project.getProjDirName() + "-results" + ".csv");
                    }
    }
                       /* // Addestra il classificatore sulla finestra di addestramento
                        classifier.buildClassifier(train);

                        // Valuta il modello sulla finestra di test
                        Evaluation evaluation = new Evaluation(train);
                        evaluation.evaluateModel(classifier, test);

                        // Ottieni l'accuratezza per questa finestra di test
                        double precision = evaluation.precision(1);
                        double recall = evaluation.recall(1);
                        double kappa = evaluation.kappa();
                        double auc = evaluation.areaUnderROC(1);
                        String aucFormatted = String.format("%.3f", auc);
                        String kappaFormatted = String.format("%.3f", kappa);
                        String precisionFormatted = String.format("%.3f", precision);
                        String recallFormatted = String.format("%.3f", recall);

                        // Stampa i risultati dell'valutazione
                        System.out.println("Finestra " + (i / stepSize + 1));
                        System.out.println("Precision: " + precisionFormatted);
                        System.out.println("Recall: " + recallFormatted);
                        System.out.println("Kappa: " + kappaFormatted);
                        System.out.println("AUC: " + aucFormatted);
                        System.out.println("----------------------------------------");*/
                    }
                //}

        //}
