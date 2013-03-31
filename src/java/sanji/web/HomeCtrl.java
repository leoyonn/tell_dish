/**
 * @(#)HomeCtrl.java, 2012-10-23. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */
package sanji.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import sanji.base.Conf;
import sanji.base.TestResult;
import sanji.flow.Modeler;
import sanji.image.base.Image;
import sanji.image.utils.ImageUtils;
import sanji.utils.Logger;
import sanji.utils.Shell;

/**
 * home page of web.
 * 
 * @author leo
 */
public final class HomeCtrl extends MultiActionController {
    private static final String VIEW_HOME = "home";

    private static final String VIEW_PICK = "pick";

    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger.info("{HomeCtrl.home}");
        return new ModelAndView(VIEW_HOME);
    }
    
    /**
     * handler for image upload
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public ModelAndView uploadImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!(request instanceof MultipartHttpServletRequest)) {
            Logger.info("{HomeCtrl.uploadImage} not a multi-part request, just return...");
            return new ModelAndView(VIEW_HOME);
        }
        Logger.info("{HomeCtrl.uploadImage} processing a multi-part request...");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile imageFile = multipartRequest.getFile("imageFile");
        Map<String, Object> model = new HashMap<String, Object>();
        if (imageFile == null) {
            model.put("error", "请指定合法的上传图片文件");
            return new ModelAndView(VIEW_HOME, model);
        }

        String filePath = Conf.IMAGE_CACHE_PATH + "img-" 
                + System.currentTimeMillis() + "-" + imageFile.getOriginalFilename();
        byte bytes[] = imageFile.getBytes();
        BufferedImage data = ImageUtils.create(bytes, filePath);
        if (data == null) {
            model.put("error", "请指定合法的上传图片文件");
            return new ModelAndView(VIEW_HOME, model);
        }
        TestResult res = Modeler.test(new Image(data, filePath), null);
        String echo = res.echo();
        Logger.info("{HomeCtrl.uploadImage} got " + res + ":" + echo);
        model.put("result", echo);
        response.setContentType("");
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print("[{");
        // json string
        writer.print("\"res\":\"" + echo + "\"");
        writer.print("}]");
        return null;
    }
    
    private static final String IMG_ROOT = "/disk1/liuyang/sanji/crawl";
    static {
        Shell.exec("ln -s " + IMG_ROOT + " ./webapps/ROOT/data", true);
    }
    
    public ModelAndView pickImages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
        String[] dishes = new File("data").list();
        model.put("dishes", dishes);
        Logger.info("{PickCtrl} dishes: " + dishes.length);
        // pick a dish, return images of that dish
        String dish = request.getParameter("dish");
        if (dish != null) {
            Logger.info("{PickCtrl} got dish: " + dish);
            String[] images = new File("data/" + dish).list();
            response.setContentType("text/javascript; charset=UTF-8");
            StringBuilder sb = new StringBuilder();
            sb.append("{\"imgs\":[");
            if (images != null) {
                for (String image: images) {
                    sb.append("{\"img\":\"data/").append(dish).append("/").append(image).append("\"},");
                }
                if (images.length > 0) {
                    sb.setLength(sb.length() - 1);
                }
            }
            sb.append("]}");
            Logger.info("{PickCtrl} dish-images: " + sb);
            response.getWriter().print(sb.toString());
            return null;
        }
        // do delete dish's images:
        String del = request.getParameter("del");
        if (del != null) {
            Logger.info("{PickCtrl} got del: " + dish);
            String[] dels = del.split(",");
            for (String file:dels) {
                new File("data/" +dish + "/" + file).delete();
                Logger.info("{PickCtrl} deleted : " + file);
            }
            return null;
        }
        return new ModelAndView(VIEW_PICK, model);
    }
    
    public static void main(String[]args) throws JSONException {
        JSONObject j = new JSONObject("{\"r\":[{\"1\":1}, {\"2\":2}]}");
        System.out.println(j);
    }
}
