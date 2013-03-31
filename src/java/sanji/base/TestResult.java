/**
 * @(#)TestResult.java, 2012-10-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Test result list.
 * @author leo
 */
public class TestResult {
    private static final int MAX_NUM = 5;

    /** use #COMPARATOR to build a min-heap */
    private PriorityQueue<ClassValue> cvQueue = new PriorityQueue<ClassValue>(MAX_NUM, COMPARATOR);
    private List<ClassValue> result = null;
    /**
     * build the result
     * @param classes
     * @param values
     * @return
     */
    public TestResult build(String[] classes, double[] values) {
        if (classes == null || values == null || classes.length != values.length) {
            throw new IllegalArgumentException();
        }
        cvQueue.clear();
        for (int i = 0; i < values.length; i ++) {
            if (values[i] > 0) {
                add(new ClassValue(classes[i], values[i]));
            }
        }
        if (cvQueue.size() == 0) {
            result = Collections.emptyList();
        }
        result = new ArrayList<ClassValue>(cvQueue.size());
        while(!cvQueue.isEmpty()) {
            result.add(cvQueue.poll());
        }
        Collections.reverse(result);
        return this;
    }
    
    private boolean add(ClassValue cv) {
        if (cvQueue.size() >= MAX_NUM) {
            if (COMPARATOR.compare(cvQueue.peek(), cv) >= 0) {
                return false;
            } else {
                cvQueue.poll();
            }
        }
        return cvQueue.add(cv);
    }
    
    @Override
    public String toString() {
        if (result == null) {
            return "[]";
        }
        return result.toString();
    }
    
    /**
     * cls: the class
     * val: class's value/weight
     * @author leo
     */
    public static class ClassValue implements Comparable<ClassValue> {

        private static final int NULL_HASH = "NULL".hashCode();

        protected String cls;

        protected double val;

        public ClassValue() {}

        /**
         * Create pair with initial values.
         * 
         * @param cls
         * @param val
         */
        public ClassValue(String cls, double val) {
            this.cls = cls;
            this.val = val;
        }

        /**
         * Return the class.
         */
        public String cls() {
            return cls;
        }

        /**
         * Set the class.
         * @param cls
         */
        public void setCls(String cls) {
            this.cls = cls;
        }

        /**
         * Return the value.
         */
        public double val() {
            return val;
        }
        
        public double val_2f() {
            return ((int)Math.round(val * 10000.0) / 100.0);
        }

        /**
         * Set the value.
         * @param val
         */
        public void setVal(double val) {
            this.val = val;
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return (cls == null ? NULL_HASH : cls.hashCode()) ^ (((Double)val).hashCode());
        }

        /**
         * Return <code>true</code> iff the two parts are both equal.
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof ClassValue)) {
                return false;
            }

            ClassValue that = (ClassValue) o;
            return ((this.cls == null && that.cls == null)
                    || (this.cls != null && this.cls.equals(that.cls)))
                    && (this.val == that.val);
        }

        @Override
        public String toString() {
            return "(" + cls + ":" + val + ")";
        }

        @Override
        public int compareTo(ClassValue that) {
            int c = Double.compare(this.val, that.val);
            return c != 0 ? c : this.cls.compareTo(that.cls);
        }
    }

    /**
     * comparator for TestResult's max heap
     */
    public static final Comparator<ClassValue> COMPARATOR = new Comparator<ClassValue>() {
        @Override
        public int compare(ClassValue o1, ClassValue o2) {
            return o1.compareTo(o2);
        }
        
    };
    
    public String echo() {
    	if (result == null || result.size() == 0 || result.get(0).val == 0) {
    		return "sanji真么有见过这东东，求指教这是虾米！";
    	}
    	ClassValue cv = result.get(0); 
        StringBuilder sb = new StringBuilder();
    	if (cv.val >= .8) {
    		sb.append("sanji有 ").append(cv.val_2f()).append("% 的把握认为这是一道").append(cv.cls).append("<br/>");
    	} else if (cv.val >= .5) {
    		sb.append("sanji赶脚这 ").append(cv.val_2f()).append("% 是道").append(cv.cls);
    		cv = result.get(1);
    		if (cv.val > .25) {
    		    sb.append("，<br/>不过也有 ").append(cv.val_2f()).append("% 的可能是").append(cv.cls).append("<br/>");
    		}
    	} else {
			sb.append("sanji有点晕了…… @.@，这个东东有");
    		for (ClassValue r: result) {
    			sb.append(r.val_2f()).append("% 的可能是").append(r.cls).append("，<br/>");
    		}
    		return sb.toString();
    	}
    	return sb.toString();
    }
}
