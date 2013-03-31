/**
 * @(#)Utils.java, 2012-10-16. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.utils;

import java.io.StringReader;
import java.text.ParseException;
import java.util.Collection;

import sanji.base.Conf;
import weka.core.Attribute;
import weka.core.converters.ArffLoader.ArffReader;


/**
 * sanji related utils.
 * @author leo
 */
public class Utils {
    /**
     * 工厂方法, 由对象 Class 创建一个 {@link ThreadLocal} 对象
     * 
     * @param clazz
     * @param initNum
     * @param maxNum
     * @return
     */
    public static <T> ThreadLocal<T> threadLocalFromClass(final Class<T> clazz) {
        return new ThreadLocal<T>(){
            @Override
            protected T initialValue() {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * get the dish-name from file-name
     * @param fileName
     * @return {@link Conf#NULL_DISH_NAME} if name contain's no 'dish-xx.*'
     */
    public static String getDishFromFileName(String fileName) {
        int s = fileName.lastIndexOf('/');
        s = s < 0 ? 0 : s;
        int t = fileName.indexOf(Conf.DISH_NAME_SPLITTER);
        if (t <= s) {
            return Conf.NULL_DISH_NAME;
        }
        return fileName.substring(s, t);
    }
    
    public static String getFileNameFromPath(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        int idx1 = path.lastIndexOf('/');
        if (idx1 < 0) {
            idx1 = 0;
        }
        int idx2 = path.lastIndexOf('\\');
        if (idx1 < idx2) {
            idx1 = idx2;
        }
        if (idx1 != 0) {
            idx1 ++;
        }
        return path.substring(idx1);
    }

    public static String getFileExtFromPath(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        int idx = path.lastIndexOf('.');
        if (idx < 0 || idx == path.length() - 1) {
            return null;
        }
        String ext = path.substring(idx);
        if (ext.length() > 5 || ext.length() < 2 || ext.contains("/") || ext.contains("\\")) {
            return null;
        }
        return ext;
    }

    /**
     * features can be single-obj or obj-arrays, sum-up all counts.
     * 
     * @param features
     * @return
     */
    public static int fullSize(Object... features) {
        if (features == null) {
            return 0;
        }
        int size = 0;
        for (Object feature: features) {
            if (feature instanceof Object[]) {
                size += ((Object[]) feature).length;
            } else if (feature instanceof Collection) {
                size += ((Collection<?>) feature).size();
            } else {
                size++;
            }
        }
        return size;
    }

    private static void classCheck(Attribute attrib, Object feature, Class<?> clazz) {
        if (!(feature.getClass().equals(clazz))) {
            onIllegalArg(attrib, feature);
        }
    }

    private static void onIllegalArg(Attribute attrib, Object feature) {
        throw new IllegalArgumentException("missmatch attrib ["
                + attrib + "] with feature [" + feature + "]!");
    }

    /**
     * compute feautre's value according to attrib.
     * @param attrib
     * @param feature
     * @return
     */
    public static double featureValue(Attribute attrib, Object feature) {
        switch (attrib.type()) {
            case Attribute.NOMINAL:
                classCheck(attrib, feature, String.class);
                int idx = attrib.indexOfValue((String)feature);
                if (idx == -1) {
                    onIllegalArg(attrib, feature);
                }
                return (double)idx;
            case Attribute.NUMERIC:
                if (feature instanceof Number) {
                    return (Double)feature;
                } else if (feature instanceof String) {
                    try {
                        return Double.valueOf((String) feature).doubleValue();
                    } catch (NumberFormatException e) {
                        // fall through to throw
                    }
                }
                onIllegalArg(attrib, feature);
            case Attribute.STRING:
                classCheck(attrib, feature, String.class);
                return attrib.addStringValue((String)feature);
            case Attribute.DATE:
                classCheck(attrib, feature, String.class);
                try {
                    return attrib.parseDate((String)feature);
                } catch (ParseException e) {
                    onIllegalArg(attrib, feature);
                }
            case Attribute.RELATIONAL:
                classCheck(attrib, feature, String.class);
                try {
                    ArffReader arff = new ArffReader(new StringReader((String) feature), attrib.relation(), 0);
                    return attrib.addRelation(arff.getData());
                } catch (Exception e) {
                    onIllegalArg(attrib, feature);
                }
            default:
                onIllegalArg(attrib, feature);
        }
        return -1; // can't get here, infact.
    }
}
