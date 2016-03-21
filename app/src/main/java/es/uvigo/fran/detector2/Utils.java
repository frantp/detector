package es.uvigo.fran.detector2;

import android.content.Context;

import java.io.File;

class Utils {
    private static String MODELS_FOLDER_NAME = "models";

    public static File modelsFolder(Context context) {
        File modelsFolder = new File(context.getFilesDir(), MODELS_FOLDER_NAME);
        if (!modelsFolder.isDirectory()) {
            modelsFolder.mkdirs();
        }
        return modelsFolder;
    }

    public static String[] modelPaths(Context context) {
        File[] files = modelsFolder(context).listFiles();
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getPath();
        }
        return paths;
    }

    public static String[] modelNames(Context context) {
        File[] files = modelsFolder(context).listFiles();
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0) {
                name = name.substring(0, pos);
            }
            names[i] = name;
        }
        return names;
    }

    public static String modelPath(Context context, String modelName) {
        return Utils.modelsFolder(context) + "/" + modelName + ".model";
    }
}
