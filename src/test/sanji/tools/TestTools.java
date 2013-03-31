/**
 * @(#)TestTools.java, 2012-10-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.tools;

import java.io.PrintWriter;

import org.junit.Assert;
import org.junit.Test;

import sanji.base.AbstractTest;

/**
 * @author leo
 */
public class TestTools extends AbstractTest {
    @Test
    public void test() {
        PrintWriter out = new PrintWriter(System.out, true);
        String[][] cmds = new String[][] {
                new String[]{"help"},
                new String[]{"x"},
                new String[]{},
                new String[]{"help", "modeler"},
                new String[]{"help", "featurer"},
                new String[]{"featurer", "bad args"},
                new String[]{"featurer", path("train"), path("train.arff")},
                new String[]{"modeler", "-train", path("train.arff"), path("j48.model")},
                new String[]{"modeler", "-test", path("test.jpg")},
                new String[]{"modeler", "-test", path("test.jpg"), path("j48.model")},
        };
        for (int i = 7; i < cmds.length; i ++) {
            try {
                new Executor().exec(cmds[i], out);
            } catch (Exception e) {
                System.out.println(e);
                Assert.assertTrue(i == 1 || i == 8); // 1: wrong cmd; 8: no j48
            }
        }
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void after() throws Exception {
    }
}
