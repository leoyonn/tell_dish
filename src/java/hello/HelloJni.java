package hello;

import sanji.base.SanjiException;
import sanji.utils.SysUtils;

public class HelloJni {
    static {
        String soPath = System.getProperty("user.dir") + "/so";
        SysUtils.addUserPath(soPath);
        try {
            System.loadLibrary("HelloJni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            throw new SanjiException("Native code library failed to load.", e);
        }
    }   

    public native static String echo(String msg);

    public static void main(String []args) {
        System.out.println(System.getProperty("file.encoding"));
        System.out.println("load done!");
        String res = echo("hello, 烟雾弹!!!");
        System.out.println("ret: " + res);
    }
}