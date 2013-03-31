/**
 * @(#)Featurer.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.flow;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.image.base.Image;
import sanji.image.feature.Gist;
import sanji.image.feature.Hist;
import sanji.image.utils.ImageUtils;
import sanji.tools.ITool;
import sanji.utils.Logger;
import sanji.utils.Shell;
import sanji.utils.Utils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * extract features of images.
 * 
 * @author leo
 */
public class Featurer implements ITool {
    /** hsv's h feature number */
    public static final int FNUM_HSV_H = 30;

    /** hsv's s feature number */
    public static final int FNUM_HSV_S = 5;

    /** hsv's v feature number */
    public static final int FNUM_HSV_V = 5;

    /** rgb's r/g/b feature number */
    public static final int FNUM_RGB_R = 10;

    /** gist feature number */
    public static final int FNUM_GIST = 768;

    /** relation of weka */
    protected static Instances RELATION;

    /** attributes of weka */
    protected static Attribute[] ATTRS;
    
    /** label's classes */
    protected static String[] LABELS;

    /** label's classes in a set for query. */
    protected static Set<String> LABEL_SET;

    public static void init() {
        // init configures, mainly REALTION, which is 'final'.
        File file = new File(Conf.WEKA_DEFINE_FILE);
        if (!file.exists()) {
            makeWekaConf();
        }
        try {
            Logger.info("{Featurer.init} load weka define from: " + file.getAbsolutePath());
            RELATION = new Instances(new BufferedReader(new FileReader(file)));
        } catch (Exception ex) { // IO
            ex.printStackTrace();
            throw new SanjiException(ex);
        }
        ATTRS = new Attribute[RELATION.numAttributes()];
        RELATION.setClassIndex(RELATION.numAttributes() - 1);
        for (int i = 0; i < RELATION.numAttributes(); i++) {
            ATTRS[i] = RELATION.attribute(i);
        }
        Enumeration<?> e = ATTRS[RELATION.classIndex()].enumerateValues();
        List<String> classes = new ArrayList<String>();
        LABEL_SET = new HashSet<String>();
        while(e.hasMoreElements()) {
            String c = (String) e.nextElement();
            if (!LABEL_SET.contains(c)) {
                LABEL_SET.add(c);
                classes.add(c);
            }
        }
        LABEL_SET.addAll(classes);
        LABELS = classes.toArray(new String[0]);
    }

    /**
     * extract all images' features in [imagePath] and write into [wekaFile].
     * 
     * @param imagePath
     * @param wekaFile
     * @return
     * @throws SanjiException
     */
    public static int extract(String imagePath, String wekaFile) throws SanjiException {
        // prepare input and output.
        FileWriter out = newWekaFile(wekaFile);
        File path = new File(imagePath);
        if (!path.exists()) {
            return -1;
        }
        List<File> files = new ArrayList<File>();
        if (path.isFile() && ImageUtils.seemsLikeAnImage(path.getName())) {
            files.add(path);
        } else if (path.isDirectory()) { // home path
            for (File sub: path.listFiles()) { // sub path
                if (sub.isDirectory()) {
                    for (File file: sub.listFiles()) { // images
                        if (ImageUtils.seemsLikeAnImage(file.getName())) {
                            files.add(file);
                        }
                    }
                }
                if (ImageUtils.seemsLikeAnImage(sub.getName())) {
                    files.add(sub);
                }
            }
        }
        if (files.size() == 0) {
            return 0;
        }
        // extract each image.
        int count = 0;
        for (File file: files) {
            try {
                String features = extract(file);
                if (features != null) {
                    out.write(features);
                    out.flush();
                    count ++;
                } else {
                    continue;
                }
            } catch (Exception ex) { // SanjiException
                Logger.warning("{Featurer.extract} got bad image [" + file + "]: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * extract features from an image
     * <b>should strictly follow weka's define: {@link Conf#WEKA_DEFINE_FILE}</b>
     * @param imageFile
     * @return
     * @throws SanjiException 
     */
    public static String extract(File imageFile) throws SanjiException {
        BufferedImage data = ImageUtils.load(imageFile);
        if (data == null) {
            Logger.info("{Featurer.extract} ignore invalid image [" + imageFile.getName() + "]...");
            return null;
        }
        Logger.info("{Featurer.extract} processing image [" + imageFile.getName() + "]...");
        Image image = new Image(data, imageFile.getName());
        return extract(image);
    }
    
    /**
     * extract features from an image.
     * <b>should strictly follow weka's define: {@link Conf#WEKA_DEFINE_FILE}</b>
     * @param image
     * @return
     * @throws SanjiException
     */
    public static String extract(Image image) throws SanjiException {
        StringBuilder sb = new StringBuilder();
        // extract features: hsvHist
        double[] hsvHist = Hist.hsvHist(image, FNUM_HSV_H, FNUM_HSV_S, FNUM_HSV_V);
        for (double f: hsvHist) {
            sb.append(String.format("%6.4f,", f));
        }
        // extract features: rgbHist
        double[] rgbHist = Hist.rgbHist(image, FNUM_RGB_R * 3);
        for (double f: rgbHist) {
            sb.append(String.format("%6.4f,", f));
        }
        // extract features: gist
        double[] gist = Gist.extract(image.pixels(), image.width(), image.height());
        for (double f: gist) {
            sb.append(String.format("%6.4f,", f));
        }
        // set label
        String label = Utils.getDishFromFileName(image.name());
        if (!LABEL_SET.contains(label)) {
            label = Conf.NULL_DISH_NAME;
        }
        sb.append(label);
        sb.append(" % ").append(image.name()).append("\n"); // file name in comment
        return sb.toString();
    }
    
    /**
     * get a weka file writer, should close after using.
     * @param filePath
     * @return
     * @throws SanjiException
     */
    protected static FileWriter newWekaFile(String filePath) throws SanjiException {
        Shell.rm(filePath);
        Shell.cp(Conf.WEKA_DEFINE_FILE, filePath);
        try {
            FileWriter wekaFile = new FileWriter(filePath, true);
            wekaFile.write("% ==== automatically added by sanji.image.Featurer ====\n");
            wekaFile.flush();
            return wekaFile;
        } catch (IOException e) {
            throw new SanjiException(e);
        }
    }
    
    /**
     * new a weka instance according to {@link Conf#WEKA_DEFINE_FILE}
     * @param features
     * @return
     */
    public static Instance newWekaInstance(Object...features) {
        if (ATTRS.length != Utils.fullSize(features)) {
            throw new IllegalArgumentException("{Featurer} features has wrong size: [" + features + "]...");
        }
        double[] wekaData = new double[ATTRS.length];
        for (int i = 0; i < wekaData.length; ) {
            Object feature = features[i];
            if (feature instanceof Object[]) {
                for (Object o: (Object[]) feature) {
                    wekaData[i] = Utils.featureValue(ATTRS[i], o);
                    i ++;
                }
            } else if (feature instanceof Collection) {
                for (Object o: (Collection<?>) feature) {
                    wekaData[i] = Utils.featureValue(ATTRS[i], o);
                    i++;
                }
            } else {
                wekaData[i] = Utils.featureValue(ATTRS[i], feature);
                i++;
            }
        }
        return new DenseInstance(1, wekaData);
    }
    
    /**
     * new a weka instance according to {@link Conf#WEKA_DEFINE_FILE}
     * 
     * @param features
     * @return
     * @throws SanjiException 
     */
    public static Instance newWekaInstance(String features) throws SanjiException {
        ArffReader arffReader = null;
        try {
            arffReader = new ArffReader(new StringReader(features), RELATION, 0);
        } catch (IOException e) {
            throw new SanjiException(e);
        }
        return arffReader.getData().get(0);
    }
    
    protected static boolean makeWekaConf() {
        Shell.mv(Conf.WEKA_DEFINE_FILE, Conf.WEKA_DEFINE_FILE + ".bak");
        StringBuilder sb = new StringBuilder();
        sb.append("% attribute dfines:")
                .append("\n%\thsvhist: ").append(FNUM_HSV_H + FNUM_HSV_S + FNUM_HSV_V)
                .append("\n%\trgbhist: ").append(FNUM_RGB_R * 3)
                .append("\n%\tgist: ").append(FNUM_GIST)
                .append("\n\n@RELATION image_features\n\n");
        for (int i = 0; i < FNUM_HSV_H; i ++) {
            sb.append("@ATTRIBUTE hsvhist_h_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_HSV_S; i ++) {
            sb.append("@ATTRIBUTE hsvhist_s_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_HSV_V; i ++) {
            sb.append("@ATTRIBUTE hsvhist_v_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_RGB_R; i ++) {
            sb.append("@ATTRIBUTE rgbhist_r_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_RGB_R; i ++) {
            sb.append("@ATTRIBUTE rgbhist_g_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_RGB_R; i ++) {
            sb.append("@ATTRIBUTE rgbhist_b_").append(i + 1).append(" NUMERIC\n");
        }
        for (int i = 0; i < FNUM_GIST; i ++) {
            sb.append("@ATTRIBUTE gist_").append(i + 1).append(" NUMERIC\n");
        }
        Shell.exec(String.format("echo \"%s\" > %s", sb.toString(), Conf.WEKA_DEFINE_FILE), true);

        // read the dishes.ist, and set as label
        Shell.cp(Conf.DISH_LIST_FILE, Conf.DISH_LIST_FILE + ".bak");
        Shell.exec(String.format("sort %s | uniq > %s", 
                Conf.DISH_LIST_FILE + ".bak", Conf.DISH_LIST_FILE), true);
        Shell.exec(String.format("awk '{x = x\",\"$1;}END{print \"%s\"substr(x, 2)\"%s\";}' %s >> %s",
                "@ATTRIBUTE label {啊哦不是菜吧,", "}\\n\\n@DATA\\n\\n", 
                Conf.DISH_LIST_FILE, Conf.WEKA_DEFINE_FILE), true);
        return true;
    }

    @Override
    public boolean exec(String[] args, PrintWriter out) {
        if (args.length == 3 && args[0].equals("-extract")) {
            return extract(args[1], args[2]) > 0;
        } else if (args.length == 1 && args[0].equals("-wekadef")) {
            return makeWekaConf();
        }
        usage(out);
        return false;
    }

    @Override
    public void usage(PrintWriter out) {
        out.println("Usage:\n 1. featurer -extract <image-path> <weka-file>");
        out.println("   (extract all images' features in <image-path> and write into <weka-file>)");
        out.println(" 2. featurer -wekadef");
        out.println("   (make the weka-define file (into pre-defined 'conf/weka_def.arff')");
    }

}
