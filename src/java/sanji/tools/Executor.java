/**
 * @(#)Executor.java, 2012-10-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.tools;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import sanji.base.Conf;
import sanji.base.SanjiException;
import sanji.flow.Featurer;
import sanji.flow.Modeler;

/**
 * tool executor
 * 
 * @author leo
 */
public class Executor {
    private static final Map<String, String> TOOLS = new TreeMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("modeler", "sanji.flow.Modeler");
            put("featurer", "sanji.flow.Featurer");
            put("biucrawler", "sanji.crawler.BiuCrawlerMaster");
            put("imagecrawler", "sanji.crawler.ImageCrawlerMaster");
            put("reseturls", "sanji.crawler.ImageCrawlerMaster");
            put("imagecrop", "sanji.image.ImageCrop");
        }
    };

    private static final Map<String, Class<? extends ITool>> TOOL_CLASSES =
            new TreeMap<String, Class<? extends ITool>>();
    
    static {
        initTools();
        Conf.init(System.getProperty("user.dir"));
        Featurer.init();
        Modeler.init();
    }

    @SuppressWarnings("unchecked")
    private static void initTools() throws SanjiException {
        for (Map.Entry<String, String> entry: TOOLS.entrySet()) {
            String toolName = entry.getKey();
            String clazzName = entry.getValue();
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzName);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                throw new SanjiException("Class Not Found: " + clazzName, ex);
            }
            if (!ITool.class.isAssignableFrom(clazz)) {
                throw new SanjiException("class is not sanji.tool.ITool: " + clazzName);
            }
            TOOL_CLASSES.put(toolName, (Class<? extends ITool>) clazz);
        }
        for (String name: TOOL_CLASSES.keySet()) {
            try {
                TOOL_CLASSES.get(name).newInstance();
            } catch (Exception ex) { // InstantiationException | IllegalAccessException
                ex.printStackTrace();
                throw new SanjiException(ex);
            }
        }
    }

    /**
     * Executes the job.
     * @param args  the arguments from the console
     * @param out the output PrintWriter
     * @return  the return value for the application
     * @throws SanjiException
     */
    public boolean exec(String[] args, PrintWriter out) throws SanjiException {
        long timeStart = System.currentTimeMillis();
        // get tool's cmd
        if (args.length == 0 || args[0].equals("help")) {
            if (args.length <= 1) {
                usage(out);
            } else {
                help(args[1], out);
                return false;
            }
            return false;
        }
        String cmd = args[0];
        Class<?> cls = TOOL_CLASSES.get(cmd);
        if (cls == null || !ITool.class.isAssignableFrom(cls)) {
            usage(out);
            throw new SanjiException("Unknown command/class: " + cmd);
        }

        // exec tool
        ITool tool = null;
        try {
            tool = (ITool) cls.newInstance();
        } catch (Exception e) {
            usage(out);
            throw new SanjiException(e);
        }
        String[] newArgs = new String[args.length - 1];
        for (int j = 0; j < newArgs.length; j++) {
            newArgs[j] = args[j + 1];
        }
        boolean res = tool.exec(newArgs, out);
        long timeEnd = System.currentTimeMillis();
        out.println("--Total elapsed time: " + (timeEnd - timeStart) + " ms ("
                + ((int) (timeEnd - timeStart) / 1000 / 60) + " minutes "
                + (((timeEnd - timeStart) / 1000) % 60) + " seconds).");
        return res;        
    }

    /**
     * Display help message for a command
     * 
     * @param cmd
     *            The command with help message
     * @param out
     * @throws SanjiException
     */
    private void help(String cmd, PrintWriter out) throws SanjiException {
        Class<?> cls = TOOL_CLASSES.get(cmd);
        if (cls == null) {
            usage(out);
            throw new SanjiException("Unknown command/class: " + cmd);
        } else {
            try {
                ((ITool) cls.newInstance()).usage(out);
            } catch (Exception e) {
                throw new SanjiException(e);
            }
        }
    }

    private void usage(PrintWriter out) {
        out.println("Usage:");
        out.println("1. help [tool-name]");
        out.println("2. <tool-name> [tool-options]");
        out.println("   tool-names can be:");
        for (String tool: TOOL_CLASSES.keySet()) {
            out.println("      " + tool + ",");
        }
        out.println("   find out of each tool using \"help [tool-name]\".\n");
    }

    public static void main(String[] args) {
        Executor exec = new Executor();
        PrintWriter out = new PrintWriter(System.out, true);
        if (!exec.exec(args, out)) {
            exec.usage(out);
        }
    }

}
