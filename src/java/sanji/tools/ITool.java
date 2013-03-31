/**
 * @(#)ITool.java, 2012-10-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.tools;

import java.io.PrintWriter;

import sanji.base.SanjiException;

/**
 * @author leo
 */
public interface ITool {
    /**
     * execute the tool.
     * @param args
     * @param out
     * @return
     * @throws SanjiException
     */
    public abstract boolean exec(String args[], PrintWriter out) throws SanjiException;

    /**
     * explain the usage of this tool and output it to <code>out</code>
     * @param out
     */
    public abstract void usage(PrintWriter out);
}
