/**
 * @(#)AbstractTable.java, 2012-11-9. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.rable;

/**
 * 用redis表进行存储
 * 非线程安全
 * @author leo
 */
public abstract class AbstractTable {
    protected byte[] enc(Object o) {
        return Serializer.encode(o);
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T dec(byte[] b, Class<T> c) {
        return (T) Serializer.decode(b, c);
    }
}
