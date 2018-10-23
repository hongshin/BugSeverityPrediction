package BugSeverityPrediction ;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File ;
import org.apache.commons.configuration.* ;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.SMO ;
import weka.classifiers.bayes.NaiveBayes ;
import weka.core.FastVector;
import weka.core.Instances;
 
public class Classification {

	public static PropertiesConfiguration loadConfig(String fname)
	{
		PropertiesConfiguration config = null ;
		try {
			config = new PropertiesConfiguration(fname) ;
		}
		catch(ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
		return config ;
	}
 

	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception 
	{
		Evaluation evaluation = new Evaluation(trainingSet);
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);
 
		return evaluation;
	}
 
	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds)
	{
		Instances[][] split = new Instances[2][numberOfFolds];
 
		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}
 
		return split;
	}

	public static void showPredictionResult(FastVector predictions) {
		double allCases = predictions.size() ;
		double truePositive = 0 ;
		double trueNegative = 0 ;
		double falsePositive = 0 ;
		double falseNegative = 0 ;
 
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				if (np.predicted() == 1.0)
					truePositive += 1.0 ;
				else
					trueNegative += 1.0 ;

			}
			else {
				if (np.predicted() == 1.0) 
					falsePositive += 1.0 ;
				else
					falseNegative += 1.0 ;
			}
		}
		System.out.println("-------------------------------------") ;
		System.out.println("Accuracy : " + ((double) (truePositive + trueNegative) / allCases * 100.0)) ;
		System.out.println("Precision: " + ((double) (truePositive) / (truePositive + falsePositive) * 100.0)) ;
		System.out.println("Recall   : " + ((double) (truePositive) / (truePositive + falseNegative) * 100.0)) ;
		System.out.println("-------------------------------------") ;
	}
 
	public static void main(String[] args) throws Exception 
	{
		PropertiesConfiguration config = loadConfig("config.properties") ;

		Instances data = new Instances(new BufferedReader(new FileReader(config.getString("arff.filename")))) ;
		
		data.setClassIndex(data.numAttributes() - 1);
 
		Instances[][] split = crossValidationSplit(data, 10);
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];
 
		
		FastVector predictions = new FastVector();

		Classifier classifier = new NaiveBayes() ;
		//Classifier classifier = new J48() ;
		//Classifier classifier = new SMO() ;
 
		for (int i = 0; i < trainingSplits.length; i++) {
			Evaluation validation = classify(classifier, trainingSplits[i], testingSplits[i]);
 			predictions.appendElements(validation.predictions());
 		}
		showPredictionResult(predictions) ;
	}
}
