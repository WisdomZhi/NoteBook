package cn.edu.gdqy.notebook;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Created by zrz on 2018/5/1.
 */

public class FileUtils {
    private final static String sizeFile = "size.properties";

    public static Properties readSizeFile(Context context) {
        Properties properties = new Properties();
        try {
//            String file = "/data/data/cn.edu.gdqy.notebook/files/size.properties";
            String path = context.getFilesDir().getAbsolutePath();
            FileInputStream fis = new FileInputStream(new File(path, sizeFile));
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void updateSizeFile(float fontSize, Context context) {
        Properties properties = new Properties();
        properties.setProperty("fontsize", fontSize+"");
        try {
            String path = context.getFilesDir().getAbsolutePath();
            FileOutputStream outputStream = new FileOutputStream(new File(path, sizeFile));
            properties.store(outputStream, "");
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkSizeFileIsExist(Context context) {
        String path = context.getFilesDir().getAbsolutePath();
        File file = new File(path,"size.properties");
        try {
            if (!file.exists()) {
                file.createNewFile();
                updateSizeFile(18, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
