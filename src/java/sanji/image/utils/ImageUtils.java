/**
 * @(#)ImageUtils.java, 2012-10-15. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import sanji.crawler.Crawler;
import sanji.image.base.Image;
import sanji.utils.Logger;
import sanji.utils.Utils;

/**
 * Image-related utils, such as load, display, etc.
 * 
 * @author leo
 */
public class ImageUtils {
    public static final Set<String> IMG_SUBS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("bmp");
            add("gif");
            add("jpeg");
            add("jpg");
            add("png");
            add("tif");
            add("tiff");
        }
    };

    /**
     * load an Image from the filePath, which can be either local or online.
     * 
     * @param filePath
     * @return
     */
    public static BufferedImage load(String filePath) {
        try {
            BufferedImage image = null;
            if (filePath.startsWith("http://")) {
                image = ImageIO.read(new URL(filePath));
            } else {
                image = ImageIO.read(new File(filePath));
            }
            return image;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * load an Image from file.
     * @param file
     * @return
     */
    public static BufferedImage load(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            Logger.severe("{ImageUtils.load} can't load ["
                    + file.getAbsolutePath() + "]: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

	/**
	 * create an image from file-bytes (not pixels!)
	 * 
	 * @param bytes
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	public static BufferedImage create(byte[] bytes, String filePath)
			throws IOException {
		// get image from bytes
		ByteArrayInputStream bais = null;
		MemoryCacheImageInputStream mcis = null;
		BufferedImage image = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			mcis = new MemoryCacheImageInputStream(bais);
			image = ImageIO.read(mcis);
		} finally {
			if (image == null && mcis != null) { // ImageIO.read will close on success.
			    mcis.close();
			}
		}
		// save bytes to file.
		if (image != null && filePath != null) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
				fos.write(bytes);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
		return image;
	}

    /**
     * create an image from pixels
     * 
     * @param pixels
     * @param w
     * @param h
     * @param fmt
     *            "JPEG", "BMP", "GIF", "PNG"...
     * @param filePath
     *            if not null, save here.
     * @return
     */
    public static BufferedImage create(int[] pixels, int w, int h, String filePath) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, w, h, pixels, 0, w);
        if (filePath != null) {
            try {
                save(image, filePath, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    /**
     * save an image to disk, use quality.
     * 
     * @param image
     * @param quality
     *            between 0 and 1.
     * @param filePath
     * @param fmt
     * @return
     * @throws IOException
     */
    public static boolean save(BufferedImage image, String filePath, float quality)
    throws IOException {	
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        String ext = Utils.getFileExtFromPath(filePath);
        if (ext == null || !IMG_SUBS.contains(ext)) {
            ext = "jpg";
        }
        Iterator<?> it = ImageIO.getImageWriters(type, ext);
        ImageWriter writer = null;
        if (it.hasNext()) {
            writer = (ImageWriter) it.next();
        }
        if (writer == null) {
            return false;
        }
        IIOImage iioImage = new IIOImage(image, null, null);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        String[] types = param.getCompressionTypes();
        if (types != null && types.length > 0) {
            param.setCompressionType(types[0]);
        }
        param.setCompressionQuality(quality);
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(new File(filePath));
        writer.setOutput(outputStream);
        writer.write(null, iioImage, param);
        return true;
    }
    
    public static boolean loadAndSave(String urlString, String savePath, int maxRetry) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.addRequestProperty("User-Agent", Crawler.FAKE_UA);
        con.setConnectTimeout(1000);
        con.setReadTimeout(2000);
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;
        int retry = 0;
        while (retry < maxRetry) {
            try {
                reader = new BufferedInputStream(con.getInputStream());
                break;
            } catch (IOException ex) {
                Logger.warning("{ImageUtils.load} retry [" + retry + "] got exception: " + ex.getMessage());
                if (++retry >= maxRetry) {
                    throw ex;
                }
            }
        }
        try {
            writer = new BufferedOutputStream(new FileOutputStream(savePath));
            byte[] buffer = new byte[4096];
            int size = 0;
            while ((size = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, size);
            }
            return true;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
    
    /**
     * scale image to new size: w x h
     * 
     * @param image
     * @param w
     * @param h
     * @return
     */
    public static BufferedImage scale(BufferedImage image, int w, int h) {
        java.awt.Image data = image.getScaledInstance(w, h, java.awt.Image.SCALE_DEFAULT);
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.getGraphics();
        g.drawImage(data, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * don't read file, just judge by name.
     * 
     * @param fileName
     * @return
     */
    public static boolean seemsLikeAnImage(String fileName) {
        int s = fileName.lastIndexOf(".");
        if (s < 0 || s >= fileName.length() - 3) {
            return false;
        }
        return (IMG_SUBS.contains(fileName.substring(s + 1)));
    }

    /**
     * read the pixels of an Image for more processing.
     * 
     * @param image
     * @return
     */
    public static int[] getPixels(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int pixels[] = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = image.getRGB(x, y);
            }
        }
        return pixels;
    }

    public static float[] rgb2hsv(int rgb) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(Image.getR(rgb), Image.getG(rgb), Image.getB(rgb), hsv);
        return hsv;
    }
    
    public static int hsv2rgb(float[] hsv) {
        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * print the string format of rgb, such as 0xFF1088
     * @param rgb
     * @return
     */
    public static String rgbString(int rgb) {
        String r = Integer.toHexString(Image.getR(rgb)).toUpperCase();
        if (r.length() == 1) {
            r = "0" + r;
        }
        String g = Integer.toHexString(Image.getG(rgb)).toUpperCase();
        if (g.length() == 1) {
            g = "0" + g;
        }
        String b = Integer.toHexString(Image.getB(rgb)).toUpperCase();
        if (b.length() == 1) {
            b = "0" + b;
        }
        return  r + g + b;
    }

    /**
     * crop an image of size * ratio in middle.
     * @param inName
     * @param outName
     * @param ratio
     * @throws IOException
     */
    public static void crop(String inName, String outName, double ratio)
    throws IOException, IllegalArgumentException { 
        ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream(inName));
        Iterator<?> readers = ImageIO.getImageReaders(iis);
        if (!readers.hasNext()) {
            return;
        }
        ImageReader reader = (ImageReader) readers.next();
        reader.setInput(iis, true, true);
        ImageReadParam param = reader.getDefaultReadParam();
        int w = reader.getWidth(0);
        int h = reader.getHeight(0);
        if (ratio > 1) {
            ratio = 1;
        }
        int size = (int) ((w > h ? h : w) * ratio);
        if (size == 0) {
            size = 1;
        }
        Rectangle rect = new Rectangle((w - size) >> 1, (h - size) >> 1, size, size);
        param.setSourceRegion(rect);
        BufferedImage bi = reader.read(0, param);
        save(bi, outName, 1.0f);
    }
    
    public static void main(String[]args) throws IOException {
//        String url = "http://t1.baidu.com/it/u=1419861424,2031025413&fm=24&gp=0.jpg";
        String url = "http://tu1.mmonly.com/mmonly/2011/201105/270/14.jpg";
        loadAndSave(url, "./tmp3.jpg", 3);
//        BufferedImage i = load(url);
//        save(i, url, "./tmp2.jpg", 1);
    }
}
