/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.bbn.openmap.omGraphics.OMGeometry;

/**
 * A set of convenience functions for deep copying data structures.
 * 
 * @author ddietrick
 */
public class DeepCopyUtil {

    public static double[] deepCopy(double[] source) {
        if (source == null) {
            return null;
        }
        double[] ds = new double[source.length];
        System.arraycopy(source, 0, ds, 0, source.length);
        return ds;
    }

    public static int[] deepCopy(int[] source) {
        if (source == null) {
            return null;
        }
        int[] ints = new int[source.length];
        System.arraycopy(source, 0, ints, 0, source.length);
        return ints;
    }

    public static boolean[] deepCopy(boolean[] source) {
        if (source == null) {
            return null;
        }
        boolean[] bools = new boolean[source.length];
        System.arraycopy(source, 0, bools, 0, source.length);
        return bools;
    }

    public static float[] deepCopy(float[] source) {
        if (source == null) {
            return null;
        }
        float[] floats = new float[source.length];
        System.arraycopy(source, 0, floats, 0, source.length);
        return floats;
    }

    public static char[] deepCopy(char[] source) {
        if (source == null) {
            return null;
        }
        char[] chars = new char[source.length];
        System.arraycopy(source, 0, chars, 0, source.length);
        return chars;
    }

    public static short[] deepCopy(short[] source) {
        if (source == null) {
            return null;
        }
        short[] shorts = new short[source.length];
        System.arraycopy(source, 0, shorts, 0, source.length);
        return shorts;
    }

    public static long[] deepCopy(long[] source) {
        if (source == null) {
            return null;
        }
        long[] longs = new long[source.length];
        System.arraycopy(source, 0, longs, 0, source.length);
        return longs;
    }

    public static byte[] deepCopy(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] bytes = new byte[source.length];
        System.arraycopy(source, 0, bytes, 0, source.length);
        return bytes;
    }

    public static <T extends OMGeometry> T deepCopy(T source) {
        T list = (T) ComponentFactory.create(source.getClass().getName());
        list.restore(source);
        return list;
    }

    public static <T extends OMGeometry> T[] deepCopy(T[] source) {
        if (source == null) {
            return null;
        }

        // This is a shallow copy, clone objects are same as source objects
        T[] clone = source.clone();
        
        // JDK 1.6 required
        //T[] clone = Arrays.copyOfRange(source, 0, source.length);

        for (int i = 0; i < source.length; i++) {
            T subclone = null;
            subclone = (T) ComponentFactory.create(source[i].getClass().getName());
            if (subclone != null) {
                subclone.restore(source[i]);
            }
            clone[i] = subclone;
        }

        return clone;
    }

    public static double[][] deepCopy(double[][] source) {
        if (source == null) {
            return null;
        }

        double[][] ret = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new double[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static boolean[][] deepCopy(boolean[][] source) {
        if (source == null) {
            return null;
        }

        boolean[][] ret = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new boolean[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static byte[][] deepCopy(byte[][] source) {
        if (source == null) {
            return null;
        }

        byte[][] ret = new byte[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new byte[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static char[][] deepCopy(char[][] source) {
        if (source == null) {
            return null;
        }

        char[][] ret = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new char[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static int[][] deepCopy(int[][] source) {
        if (source == null) {
            return null;
        }

        int[][] ret = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new int[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static float[][] deepCopy(float[][] source) {
        if (source == null) {
            return null;
        }

        float[][] ret = new float[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new float[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static long[][] deepCopy(long[][] source) {
        if (source == null) {
            return null;
        }

        long[][] ret = new long[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new long[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static short[][] deepCopy(short[][] source) {
        if (source == null) {
            return null;
        }

        short[][] ret = new short[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new short[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }
    /**
     * Utility for making deep copies (vs. clone()'s shallow copies) of objects. Objects are first serialized and then
     * deserialized. Error checking is fairly minimal in this implementation. If an object is encountered that cannot be
     * serialized (or that references an object that cannot be serialized) an error is printed to System.err and null is
     * returned. Depending on your specific application, it might make more sense to have copy(...) re-throw the exception.
     * 
     * (http://javatechniques.com/public/java/docs/basics/faster-deep-copy.html)
     */
    /**
     * Returns a copy of the object, or null if the object cannot be serialized.
     */
    public static Object copy(Object orig) throws Exception {
        Object obj = null;
        // Write the object out to a byte array
        FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(fbos);
        out.writeObject(orig);
        out.flush();
        out.close();

        // Retrieve an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
        obj = in.readObject();
        
        return obj;
    }
}


/**
 * ByteArrayOutputStream implementation that doesn't synchronize methods
 * and doesn't copy the data on toByteArray().
 * (http://javatechniques.com/public/java/docs/basics/faster-deep-copy.html)
 */
class FastByteArrayOutputStream extends OutputStream {
    /**
     * Buffer and size
     */
    protected byte[] buf = null;
    protected int size = 0;

    /**
     * Constructs a stream with buffer capacity size 5K 
     */
    public FastByteArrayOutputStream() {
        this(5 * 1024);
    }

    /**
     * Constructs a stream with the given initial size
     */
    public FastByteArrayOutputStream(int initSize) {
        this.size = 0;
        this.buf = new byte[initSize];
    }

    /**
     * Ensures that we have a large enough buffer for the given size.
     */
    private void verifyBufferSize(int sz) {
        if (sz > buf.length) {
            byte[] old = buf;
            buf = new byte[Math.max(sz, 2 * buf.length )];
            System.arraycopy(old, 0, buf, 0, old.length);
            old = null;
        }
    }

    public int getSize() {
        return size;
    }

    /**
     * Returns the byte array containing the written data. Note that this
     * array will almost always be larger than the amount of data actually
     * written.
     */
    public byte[] getByteArray() {
        return buf;
    }

    public final void write(byte b[]) {
        verifyBufferSize(size + b.length);
        System.arraycopy(b, 0, buf, size, b.length);
        size += b.length;
    }

    public final void write(byte b[], int off, int len) {
        verifyBufferSize(size + len);
        System.arraycopy(b, off, buf, size, len);
        size += len;
    }

    public final void write(int b) {
        verifyBufferSize(size + 1);
        buf[size++] = (byte) b;
    }

    public void reset() {
        size = 0;
    }

    /**
     * Returns a ByteArrayInputStream for reading back the written data
     */
    public InputStream getInputStream() {
        return new FastByteArrayInputStream(buf, size);
    }

}

/**
 * ByteArrayInputStream implementation that does not synchronize methods.
 * 
 * (http://javatechniques.com/public/java/docs/basics/faster-deep-copy.html)
 **/
class FastByteArrayInputStream extends InputStream {
    /**
     * Our byte buffer
     **/
    protected byte[] buf = null;

    /**
     * Number of bytes that we can read from the buffer
     **/
    protected int count = 0;

    /**
     * Number of bytes that have been read from the buffer
     **/
    protected int pos = 0;

    public FastByteArrayInputStream(byte[] buf, int count) {
        this.buf = buf;
        this.count = count;
    }

    public final int available() {
        return count - pos;
    }

    public final int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    public final int read(byte[] b, int off, int len) {
        if (pos >= count)
            return -1;

        if ((pos + len) > count)
            len = (count - pos);

        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    public final long skip(long n) {
        if ((pos + n) > count)
            n = count - pos;
        if (n < 0)
            return 0;
        pos += n;
        return n;
    }

}
