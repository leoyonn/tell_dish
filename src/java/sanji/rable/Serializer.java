/**
 * @(#)Serializer.java, 2012-11-13.
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */

package sanji.rable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * serializer for all java objects.
 * @author leo
 */
public class Serializer {

    public static final int DEFAULT_COMPRESSION_THRESHOLD = 16384;

    public static final String DEFAULT_CHARSET = "UTF-8";

    protected final static boolean packZeros = true;

    protected static int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;

    protected static final String charset = DEFAULT_CHARSET;

    public static byte[] encode(Object o) {
        if (o == null) {
            return null;
        }
        try {
            byte[] b = null;
            if (o instanceof String) {
                b = encodeString((String) o);
            } else if (o instanceof Long) {
                b = encodeLong((Long) o);
            } else if (o instanceof Integer) {
                b = encodeInt((Integer) o);
            } else if (o instanceof Boolean) {
                b = encodeBoolean((Boolean) o);
            } else if (o instanceof Byte) {
                b = encodeByte((Byte) o);
            } else if (o instanceof Float) {
                b = encodeInt(Float.floatToRawIntBits((Float) o));
            } else if (o instanceof Double) {
                b = encodeLong(Double.doubleToRawLongBits((Double) o));
            } else if (o instanceof byte[]) {
                b = (byte[]) o;
            } else {
                b = serialize(o);
            }
            assert b != null;
            // TODO: 璇诲彇鐨勬椂鍊欏浣曠煡閬撴槸鍚﹀帇缂╄繃
            // if (b.length > compressionThreshold) {
            // byte[] compressed = compress(b);
            // if (compressed.length < b.length) {
            // b = compressed;
            // }
            // }
            return b;
        } catch (Exception ex) {
            throw new RuntimeException("encode object [" + o
                    + "] to bytes throws exception:" + ex.getMessage());
        }
    }

    public static Object decode(byte[] bytes, Class<?> clazz) {
        Object rv = null;
        if (bytes == null) {
            return null;
        }
        try {
            byte[] data = bytes;// TODO: 鍘嬬缉涓庤В鍘嬬缉鐨勫搴�decompress(bytes);
            if (clazz.equals(String.class)) {
                rv = decodeString(data);
            } else if (clazz.equals(Long.class)) {
                rv = decodeLong(data);
            } else if (clazz.equals(Integer.class)) {
                rv = decodeInt(data);
            } else if (clazz.equals(Boolean.class)) {
                rv = decodeBoolean(data);
            } else if (clazz.equals(Byte.class)) {
                rv = decodeByte(data);
            } else if (clazz.equals(Float.class)) {
                rv = new Float(Float.intBitsToFloat(decodeInt(data)));
            } else if (clazz.equals(Double.class)) {
                rv = new Double(Double.longBitsToDouble(decodeLong(data)));
            } else if (clazz.equals(byte[].class)) {
                rv = data;
            } else {
                rv = deserialize(data);
            }
            return rv;
        } catch (Exception ex) {
            throw new RuntimeException("decode bytes [" + Arrays.toString(bytes)
                    + "] to Class [" + clazz + "] throws exception:"
                    + ex.getMessage());
        }
    }

    /**
     * Decode the string with the current character set.
     */
    public static String decodeString(byte[] data) {
        String rv = null;
        try {
            if (data != null) {
                rv = new String(data, charset);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return rv;
    }

    /**
     * Encode a string into the current character set.
     */
    public static byte[] encodeString(String in) {
        byte[] rv = null;
        try {
            rv = in.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return rv;
    }

    /**
     * encode a number(integer or long)
     * @param l
     * @param maxBytes
     * @return
     */
    private static byte[] encodeNum(long number, int maxBytes) {
        byte[] rv = new byte[maxBytes];
        for (int i = 0; i < rv.length; i++) {
            int pos = rv.length - i - 1;
            rv[pos] = (byte) ((number >> (8 * i)) & 0xff);
        }
        if (packZeros) {
            int firstNon0 = 0;
            for (; firstNon0 < rv.length && rv[firstNon0] == 0; firstNon0++) {}
            if (firstNon0 > 0) {
                byte[] tmp = new byte[rv.length - firstNon0];
                System.arraycopy(rv, firstNon0, tmp, 0, rv.length - firstNon0);
                rv = tmp;
            }
        }
        return rv;
    }

    /**
     * encode a long number
     * @param l
     * @return
     */
    public static byte[] encodeLong(long l) {
        return encodeNum(l, 8);
    }

    /**
     * decode a long number
     * @param b
     * @return
     */
    public static long decodeLong(byte[] b) {
        long rv = 0;
        for (byte i: b) {
            rv = (rv << 8) | (i < 0 ? 256 + i : i);
        }
        return rv;
    }

    /**
     * encode an integer
     * @param in
     * @return
     */
    public static byte[] encodeInt(int in) {
        return encodeNum(in, 4);
    }

    /**
     * decode an integer
     * @param in
     * @return
     */
    public static int decodeInt(byte[] in) {
        assert in.length <= 4: "Too long to be an int (" + in.length
                + ") bytes";
        return (int) decodeLong(in);
    }

    public static byte[] encodeByte(byte in) {
        return new byte[] {
            in
        };
    }

    public static byte decodeByte(byte[] in) {
        assert in.length <= 1: "Too long for a byte";
        byte rv = 0;
        if (in.length == 1) {
            rv = in[0];
        }
        return rv;
    }

    public static byte[] encodeBoolean(boolean b) {
        byte[] rv = new byte[1];
        rv[0] = (byte) (b ? '1' : '0');
        return rv;
    }

    public static boolean decodeBoolean(byte[] in) {
        assert in.length == 1: "Wrong length for a boolean";
        return in[0] == '1';
    }

    /**
     * Get the bytes representing the given serialized object.
     */
    public static byte[] serialize(Object o) {
        if (o == null) {
            throw new NullPointerException("Can't serialize null");
        }
        byte[] rv = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(o);
            os.close();
            bos.close();
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
        return rv;
    }

    /**
     * Get the object represented by the given serialized bytes.
     */
    public static Object deserialize(byte[] bytes) {
        Object object = null;
        try {
            ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
            ObjectInputStream os = new ObjectInputStream(bos);
            object = os.readObject();
            return object;
        } catch (Exception ex) {
            throw new RuntimeException("decode bytes [" + Arrays.toString(bytes)
                    + "] to Object throws exception:" + ex.getMessage());
        }
    }

    public static byte[] mEncode(Object... objects) {
        if (objects == null) {
            throw new NullPointerException("Can't serialize null");
        }
        byte[] rv = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            for (Object o: objects) {
                os.writeObject(o);
            }
            os.close();
            bos.close();
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
        return rv;
    }

    public static Object[] mDecode(byte[] bytes) {
        try {
            ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
            ObjectInputStream os = new ObjectInputStream(bos);
            ArrayList<Object> olist = new ArrayList<Object>();
            Object object = null;
            try {
                while (null != (object = os.readObject())) {
                    olist.add(object);
                }
            } catch (java.io.EOFException ex) {
                // fall through
            } finally {
                bos.close();
                os.close();
            }
            return olist.toArray(new Object[olist.size()]);
        } catch (Exception ex) {
            throw new RuntimeException("decode bytes [" + Arrays.toString(bytes)
                    + "] to Object throws exception:" + ex.getMessage());
        }
    }

    /**
     * Compress the given array of bytes.
     */
    public static byte[] compress(byte[] in) {
        if (in == null) {
            throw new NullPointerException("Can't compress null");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gz = null;
        try {
            gz = new GZIPOutputStream(bos);
            gz.write(in);
        } catch (IOException e) {
            throw new RuntimeException("IO exception compressing data", e);
        } finally {
            try {
                if(gz != null) {
                    gz.close();
                }
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }

    /**
     * Decompress the given array of bytes.
     * 
     * @return null if the bytes cannot be decompressed
     */
    public static byte[] decompress(byte[] in) {
        ByteArrayOutputStream bos = null;
        if (in != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            bos = new ByteArrayOutputStream();
            GZIPInputStream gis;
            try {
                gis = new GZIPInputStream(bis);

                byte[] buf = new byte[8192];
                int r = -1;
                while ((r = gis.read(buf)) > 0) {
                    bos.write(buf, 0, r);
                }
            } catch (IOException e) {
                bos = null;
            }
        }
        return bos == null ? null : bos.toByteArray();
    }

    /**
     * 灏嗕竴涓猟ouble鐨勫瓧鑺傛祦杞垚long
     * 
     * @param v
     * @return
     */
    public static double bytesLong2Double(long v) {
        return (Double) decode(encode(v), Double.class);
    }

    /**
     * 灏嗕竴涓猯ong鐨勫瓧鑺傛祦杞垚double
     * 
     * @param v
     * @return
     */
    public static long bytesDouble2Long(double v) {
        return (Long) decode(encode(v), Long.class);
    }
}
