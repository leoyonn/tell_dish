/**
 * @(#)ImageCrop.java, 2012-11-19. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.image;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import sanji.base.SanjiException;
import sanji.image.utils.ImageUtils;
import sanji.tools.ITool;
import sanji.utils.Logger;

/**
 * @author leo
 *
 */
public class ImageCrop implements ITool {
    @Override
    public boolean exec(String[] args, PrintWriter out) throws SanjiException {
        if (args.length == 3) {
            try {
                if (crop(args[1], args[2], Double.valueOf(args[0]))) {
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        usage(out);
        return false;
    }
    
    private boolean crop(String inPath, String outPath, double ratio) {
        // 1. prepare inputs and outputs
        File in = new File(inPath);
        File out = new File(outPath);
        List<String> inputs = new ArrayList<String>();
        List<String> outputs = new ArrayList<String>();
        if (in.isDirectory()) {
            for (File file: in.listFiles()) {
                if (file.isDirectory()) {
                    new File(out.getAbsolutePath() + "/" + file.getName()).mkdirs();
                    for (File inner: file.listFiles()) {
                        inputs.add(in.getAbsolutePath() + "/" + file.getName() + "/" + inner.getName());
                        outputs.add(out.getAbsolutePath() + "/" + file.getName() + "/" + inner.getName());
                    }
                } else {
                    inputs.add(in.getAbsolutePath() + "/" + file.getName());
                    outputs.add(out.getAbsolutePath() + "/" + file.getName());
                }
            }
        } else {
            inputs.add(in.getAbsolutePath());
            outputs.add(out.getAbsolutePath());
        }
        
        // 2. do crop
        for (int i = 0; i < inputs.size(); i ++) {
            try {
                ImageUtils.crop(inputs.get(i), outputs.get(i), ratio);
            } catch (Exception e) {
                Logger.warning("{ImageCrop} " + inputs.get(i) + " to "
                        + outputs.get(i) + " failed: " + e.getMessage());
            }
            Logger.info("{ImageCrop} " + inputs.get(i) + " to "
                    + outputs.get(i) + " by ratio: " + ratio + " done...");
        }
        return true;
    }

    @Override
    public void usage(PrintWriter out) {
        out.println("Usage:\n 1. imagecrop ratio <input-image-path> <output-image-path>");
        out.println("   (crop all images' in <input-image-path> by ratio(0-1)");
        out.println("    and write into <output-image-path>)");
    }
    
}
