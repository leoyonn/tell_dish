/**
 * @(#)Image.java, 2012-10-15. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.base;

import java.awt.image.BufferedImage;
import java.io.IOException;

import sanji.image.utils.ImageUtils;
import sanji.utils.Utils;

/**
 * 图像类
 * 
 * @author leo
 */
public class Image {
    private BufferedImage data;
    
    private int[] pixels;

    private int w;

    private int h;
    
    private String name;
    
    /**
     * load an Image from the filePath, which can be either local or online.
     * 
     * @param filePath
     * @return
     * @throws IOException 
     */
    public Image(String filePath) throws IOException {
        BufferedImage data = ImageUtils.load(filePath);
        if (data == null) {
            throw new IOException("Can't load image from path [" + filePath + "]...");
        }
        set(data);
        this.name = Utils.getFileNameFromPath(filePath);
    }
    
    public Image(BufferedImage data, String name) {
        set(data);
        this.name = name;
    }
    
    private void set(BufferedImage data) {
        this.data = data;
        this.w = data.getWidth();
        this.h = data.getHeight();
        this.pixels = ImageUtils.getPixels(data);
    }

    public int[] pixels() {
        return pixels;
    }

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }
    
    public int pixel(int x, int y) {
        return pixels[y * w + x];
    }
    
    public void setPixel(int pixel, int x, int y) {
        this.pixels[y * w + x] = pixel;
    }
    
    public BufferedImage data() {
        return data;
    }

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
     * get the r from rgb
     * 
     * @param rgb
     * @return
     */
    public static int getR(int rgb) {
        return (rgb & 0xff0000) >> 16;
    }

    /**
     * get the g from rgb
     * 
     * @param rgb
     * @return
     */
    public static int getG(int rgb) {
        return (rgb & 0xff00) >> 8;
    }

    /**
     * get the b from rgb
     * 
     * @param rgb
     * @return
     */
    public static int getB(int rgb) {
        return (rgb & 0xff);
    }
    
    public static int rgb(int r, int g, int b) {
        return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff); 
    }
    
    public Image scale(int w, int h) {
        return new Image(ImageUtils.scale(data, w, h), name);
    }
}
