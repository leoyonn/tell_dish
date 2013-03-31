/**
 * @(#)Shell.java, 2012-4-1. 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author leo
 */
public class Shell {
    /**
     * run a system command
     * 
     * @param cmd       瑕佹墽琛岀殑command锛屽ls/grep x y绛�     * @param print     鏄惁鎵撳嵃澶勭悊缁撴灉
     * @return
     * @throws IOException
     */
    public static boolean exec(String cmd, boolean print) {
        BufferedReader inReader = null;
        BufferedReader errReader = null;
        try {
            Process pid = null;
            // 鎵цShell鍛戒护
            String []cmds = {"/bin/bash", "-c", cmd};
            pid = Runtime.getRuntime().exec(cmds);
            if (pid != null) {
                if (print) {
                    inReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
                    errReader = new BufferedReader(new InputStreamReader(pid.getErrorStream()), 1024);
                }
                pid.waitFor();
            }
            System.out.println("{Shell}.commend锛� + cmd);
            System.out.println("{Shell}.result锛歕n");
            String line = null;
            // 璇诲彇Shell鐨勮緭鍑哄唴瀹癸紝骞舵坊鍔犲埌stringBuffer涓�            while (inReader != null && (line = inReader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("{Shell}.error锛歕n");
            while (errReader != null && (line = errReader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("{Shell}.finished锛乗r\n");
            return true;
        } catch (Exception ioe) {
            System.out.println("{Shell} got exception锛� + ioe.getMessage());
        } finally {
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    public static boolean mv(String src, String des) {
        return exec("mv " + src + " " + des, true);
    }

    public static boolean cp(String src, String des) {
        return exec("cp " + src + " " + des, true);
    }

    public static boolean rm(String file) {
        return exec("rm " + file, true);
    }

    public static boolean sort(String srcfile, String params) {
        return exec("sort " + params + " " + srcfile, true);
    }

    public static boolean mkdir(String dir) {
        return exec("mkdir " + dir, true);
    }

    public static void main(String[]args) throws IOException {
        exec("openssl smime -binary -sign -signer certificate.pem -inkey key.pem -in sup.sh -out sig -outform DER", true);
    }

}
