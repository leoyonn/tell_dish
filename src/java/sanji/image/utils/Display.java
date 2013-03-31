/**
 * @(#)Display.java, 2012-10-15. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image.utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import sanji.utils.Shell;

/**
 * display image or hist.
 * 
 * @author leo
 */
public class Display {

    private static final int HIST_WIDTH = 15;// 柱形图的宽度

    private static final int HIST_GAP = 10;// 柱形图的间距

    public static final Color[] DEF_COLORS = new Color[] {
        Color.RED, Color.GREEN, Color.BLUE, Color.CYAN,
        Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW,
    };

    /**
     * open a window and display an Image.
     * 
     * @param filePath
     * @param timeToDisplay
     *            millseconds to display this image
     */
    public static void showImage(String filePath, long timeToDisplay) {
        Image image = ImageUtils.load(filePath);
        if (image != null) {
            // Use a label to display the image
            JFrame frame = new JFrame();
            JLabel label = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(label, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                Thread.sleep(timeToDisplay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.dispose();
        }
    }
    
    /**
     * show the hist of v
     * @param v
     * @return
     */
    public static BufferedImage showHist(double[] v) {
        return showHist(v, null, null, null, 1000);
    }
    
    /**
     * show the hist of v.
     * 
     * @param v
     * @param binTitles
     * @param colors
     * @param title
     * @return
     */
    public static BufferedImage showHist(double[] v, String[] binTitles, Color[] colors, String title) {
        return showHist(v, binTitles, colors, title, 1000);
    }

    /**
     * show the hist of v.
     * 
     * @param v
     * @param binTitles
     *            can be null
     * @param color
     *            can be null
     * @param title
     * @param mills
     *            how long would the hist being shown
     */
    public static BufferedImage showHist(double[] v, String[] binTitles, Color[] colors,
            String title, long mills) {
        if (title == null) {
            title = "直方图";
        }
        // fill the titles
        if (binTitles == null) {
            binTitles = new String[v.length];
            for (int i = 0; i < v.length; i ++) {
                binTitles[i] = String.valueOf(i + 1);
            }
        }
        // fill colors
        if (colors == null) {
            colors = DEF_COLORS;
        }
        int width = v.length * HIST_WIDTH + v.length * HIST_GAP + 50;
        int height = 256, bottom = 24;
        // 1. calculate the scale so to contains all v
        double scale = calculateScale(v, height - bottom);
        // 2. create image and draw title
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   
        FontMetrics metrics = null;
        g.setFont(new Font(null, Font.BOLD, 18));
        g.setColor(Color.RED);
        g.drawString(title, (image.getWidth() - g.getFontMetrics().stringWidth(title)) >> 1, height - 6);

        // 3. draw the axis
        g.setFont(new Font(null, Font.PLAIN, 12));
        metrics = g.getFontMetrics();
        g.setColor(Color.BLACK);
        g.drawLine(10, 0, 10, height - 15 - bottom); // x
        g.drawLine(10, height - 15 - bottom, width, height - 15 - bottom); // y

        // 4. draw each bin
        for (int i = 0; i < v.length; ++i) {
            // set foreground color
            g.setColor(colors[i % colors.length]);

            // get x & y
            int x = 20 + i * (HIST_GAP + HIST_WIDTH);
            int y = height - bottom - 16 - (int) (v[i] * scale);

            // get the scale
            g.drawString(v[i] + "", x - ((metrics.stringWidth(v[i] + "") - HIST_WIDTH) >> 1), y);

            // draw rect of bin
            g.drawRect(x, y, HIST_WIDTH, (int) (v[i] * scale));
            g.fillRect(x, y, HIST_WIDTH, (int) (v[i] * scale));

            // draw bin's title
            g.drawString(binTitles[i], x - ((metrics.stringWidth(binTitles[i]) - HIST_WIDTH) >> 1), height - bottom);
        }
        g.dispose();
        String path = System.getProperty("user.dir") + "/hist/";
        Shell.mkdir(path);
        String fileName = path + "hist-" + title + ".jpg";
        try {
            ImageUtils.save(image, fileName, 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        showImage(fileName, mills);
        return image;
    }

    /**
     * calculate the scale of image due to v's max height.
     * 
     * @param v
     * @param h
     *            the height of image to display.
     * @return
     */
    private static double calculateScale(double[] v, int h) {
        double max = Double.MIN_VALUE;
        for (int i = 0, len = v.length; i < len; ++i) {
            if (v[i] > max) {
                max = v[i];
            }
        }
        return (h - 40) / max;
    }
}
