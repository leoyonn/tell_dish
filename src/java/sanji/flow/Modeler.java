/**
 * @(#)Model.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.flow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Random;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.base.TestResult;
import sanji.image.base.Image;
import sanji.tools.ITool;
import sanji.utils.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * train model using features got.
 * 
 * @author leo
 */
public class Modeler implements ITool {
    /** the j48 tree model */
    private static J48 MODEL_J48;
    /** j48 tree model path */
    private static String MODEL_J48_PATH = Conf.path("/data/default.model");
    
    /**
     * load trained model, if no model found, go train one offline.
     * (use {@link #train}.
     */
    public static void init() {
        MODEL_J48_PATH = Conf.path("/data/default.model");
        Logger.info("{Modeler.init} load model from: " + MODEL_J48_PATH);
        try {
            MODEL_J48 = (J48) loadModel(MODEL_J48_PATH);
        } catch (Exception ex) {
            Logger.warning(MODEL_J48_PATH + " not found. maybe haven't trained one.");
        }
    }

    /**
     * demo for using weka's cluster
     * 
     * @param arffFileName
     * @throws Exception
     */
    public static void clusterDemo(String arffFileName) throws Exception {
        ClusterEvaluation eval;
        Instances data;
        String[] options;
        DensityBasedClusterer cl;

        data = new Instances(new BufferedReader(new FileReader(arffFileName)));

        // normal
        System.out.println("\n--> normal");
        options = new String[2];
        options[0] = "-t";
        options[1] = arffFileName;
        System.out.println(ClusterEvaluation.evaluateClusterer(new EM(), options));

        // manual call
        System.out.println("\n--> manual");
        cl = new EM();
        cl.buildClusterer(data);
        eval = new ClusterEvaluation();
        eval.setClusterer(cl);
        eval.evaluateClusterer(new Instances(data));
        System.out.println("# of clusters: " + eval.getNumClusters());

        // density based
        System.out.println("\n--> density (CV)");
        cl = new EM();
        eval = new ClusterEvaluation();
        eval.setClusterer(cl);
        ClusterEvaluation.crossValidateModel(cl, data, 10, data.getRandomNumberGenerator(1));
        System.out.println("# of clusters: " + eval.getNumClusters());
    }

    /**
     * train a model use weka arff file
     * (can get from {@link Featurer#extract(String, String)})
     * 
     * @param arffFileName
     * @param modelPath
     * @throws Exception
     */
    public static void train(String arffFileName, String modelPath) throws Exception {
        Logger.info("{Train} loading instances...");
        Instances instances = new Instances(new BufferedReader(new FileReader(arffFileName)));
        instances.setClassIndex(instances.numAttributes() - 1);
        Logger.info("{Train} got [" + instances.size() + "] instances, train use j48...");
        
        J48 j48 = new J48();
        j48.setOptions(new String[] {"-U"});
        j48.buildClassifier(instances);
        saveModel(j48, modelPath);
        Logger.info("{Train} done, save model to [" + modelPath + "]:\n" + j48);
    }
    
    public static void crossValidation(String arffFileName) throws Exception {
        Logger.info("{Train.crossValidation} loading instances...");
        Instances instances = new Instances(new BufferedReader(new FileReader(arffFileName)));
        instances.setClassIndex(instances.numAttributes() - 1);
        Logger.info("{Train} got [" + instances.size() + "] instances, evaluate use j48...");
        
        Evaluation eval = new Evaluation(instances);
        J48 j48 = new J48();
        j48.setOptions(new String[] {"-U"});
        eval.crossValidateModel(new J48(), instances, 10, new Random());
        Logger.info("{Train} evaluation done, toSummaryString: " + eval.toSummaryString());
        Logger.info("{Train} evaluation done, toClassDetailsString: " + eval.toClassDetailsString());
    }

    /**
     * test instance use model, if model is null, use default.
     * 
     * @param instance
     * @param model
     * @return null if got exception.
     * @throws SanjiException
     */
    public static TestResult test(Instance instance, Classifier model) throws SanjiException {
        // check model
        if (model == null) {
            if (MODEL_J48 == null) {
                try {
                    MODEL_J48 = (J48) loadModel(MODEL_J48_PATH);
                } catch (Exception ex) {
                    throw new SanjiException(MODEL_J48_PATH + " not found. maybe haven't trained one.", ex);
                }
            }
            model = MODEL_J48;
        }

        // do classify.
        try {
            instance.setClassMissing();
            // double value = model.classifyInstance(instance);
            System.out.println(instance);
            double[]values = model.distributionForInstance(instance);
            return new TestResult().build(Featurer.LABELS, values);
        } catch (Exception ex) {
            Logger.severe("{Model.test} got [" + ex.getMessage()
                    + "] when test [" + instance + "] use:" + model);
            throw new SanjiException("test failed", ex);
        }
    }
    
    /**
     * test image by model
     * @param imageFileName
     * @param modelFileName
     * @return
     * @throws SanjiException
     */
    public static TestResult test(String imageFileName, String modelFileName) throws SanjiException {
        File imageFile = new File(imageFileName);
        Classifier model = null;
        if (modelFileName != null) {
            try {
                model = (Classifier) loadModel(modelFileName);
            } catch (SanjiException ex) {
                try {
                    MODEL_J48 = (J48) loadModel(MODEL_J48_PATH);
                    model = MODEL_J48;
                } catch (SanjiException ex2) {
                    throw new SanjiException("j48.model not found. maybe haven't trained one.", ex2);
                }
            }
        }
        return test(imageFile, model);
    }
    
    /**
     * test image by model.
     * @param imageFile
     * @param model
     * @return
     */
    public static TestResult test(Image image, Classifier model) {
        String feature = Featurer.extract(image);
        Instance instance = Featurer.newWekaInstance(feature);
        return test(instance, model);
    }
    
    /**
     * test image by model.
     * @param imageFile
     * @param model
     * @return
     */
    public static TestResult test(File imageFile, Classifier model) {
        String feature = Featurer.extract(imageFile);
        Instance instance = Featurer.newWekaInstance(feature);
        return test(instance, model);
    }
    
    /**
     * save trained model for further usage.
     * @param model
     * @param modelPath
     * @throws SanjiException
     */
    public static void saveModel(Object model, String modelPath) throws SanjiException {
        try {
            SerializationHelper.write(modelPath, model);
        } catch (Exception e) {
            throw new SanjiException("save model from [" + modelPath + "] got exception", e);
        }
    }
    
    /**
     * load trained model from local path.
     * @param modelPath
     * @return
     * @throws SanjiException
     */
    public static Object loadModel(String modelPath) throws SanjiException {
        try {
            Object model = SerializationHelper.read(modelPath);
            Logger.info("{Train.loadModel} from [" + modelPath + "] success.");
            return model;
        } catch (Exception e) {
            throw new SanjiException("load model from [" + modelPath + "] got exception", e);
        }
    }
    
    @Override
    public boolean exec(String[] args, PrintWriter out) throws SanjiException {
        if (args.length == 3 && args[0].equals("-train")) {
            try {
                train(args[1], args[2]);
                return true;
            } catch (Exception e) {
                throw new SanjiException(e);
            }
        } else if (args.length >= 2 && args[0].equals("-test")) {
            TestResult res = null;
            if (args.length == 2) {
                res = test(args[1], null);
            } else {
                res = test(args[1], args[2]);
            }
            out.println("TestResult: " + res);
            return res != null;
        } else if (args.length == 2 && args[0].equals("-eval")) {
            try {
                crossValidation(args[1]);
                return true;
            } catch (Exception e) {
                throw new SanjiException(e);
            }
        }
        usage(out);
        return false;
    }

    @Override
    public void usage(PrintWriter out) {
        out.println("Usage:\n   1. modeler -train <weka-file> <model-file>");
        out.println("      (train using <weka-file>, and write to <model-file>)");
        out.println("   2. modeler -test <image-file> [<model-file>]");
        out.println("      (test <image-file>, [<model-file>] can be unset)");
        out.println("   3. modeler -eval [<weka-file>]");
        out.println("      (evaluate the model using <weka-file>)");
    }
}
