/**
 * @(#)SysUtils.java, 2012-9-29. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.utils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * system related utils.
 * 
 * @author Leo
 */
public class SysUtils {
    /**
     * add a new path to user_paths('java.library.path') of ClassLoader. This is to resolve a bug of jvm:
     * user_paths can't be set after init.
     * @see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4280189
     * 
     * @param path
     * @throws IOException
     */
    public static boolean addUserPath(String path) {  
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (path.equals(paths[i])) {
                    return true;
                }
            }
            String[] newPaths = new String[paths.length + 1];
            System.arraycopy(paths, 0, newPaths, 0, paths.length);
            newPaths[paths.length] = path;
            field.set(null, newPaths);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
