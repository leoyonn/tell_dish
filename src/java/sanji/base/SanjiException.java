/**
 * @(#)SanjiException.java, 2012-10-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.base;

/**
 * @author leo
 */
public class SanjiException extends RuntimeException {
    public SanjiException() {
        super();
    }

    /**
     * @param msg
     */
    public SanjiException(String msg) {
        super(msg);
    }
    
    /**
     * @param ex
     */
    public SanjiException(Exception ex) {
        super(ex);
    }

    /**
     * @param msg
     * @param ex
     */
    public SanjiException(String msg, Throwable ex) {
        super(msg, ex);
    }

    private static final long serialVersionUID = 1L;
}
