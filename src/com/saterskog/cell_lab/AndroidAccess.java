package com.saterskog.cell_lab;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class AndroidAccess extends Accessor{
    public static enum Type{
        PARCEL
    }

    private Type accessType;

    protected AndroidAccess(Object obj, Type type) {
        super(obj);
        this.accessType = type;
    }

    protected Type getType(){
        return this.accessType;
    }

    public static class Fragment extends Accessor<Object>{
        protected Fragment(Object fragmentRef){
            super(fragmentRef);
        }

        public AppActivity getActivity(){
            return getFragmentActivity(this.getObjectReference());
        }
    }

    public static class AppActivity extends Accessor<Object>{
        protected AppActivity(Object activity){
            super(activity);
        }
    }

    // Reflection utilities for the Android API

    //Fully qualified class names of various android utils for dynamic class loading
    private static final String CONTEXT_CLASS = "android.content.Context";
    private static final String DEX_CLASS_LOADER_CLASS = "dalvik.system.DexClassLoader";
    private static final String ASSET_MANAGER_CLASS = "android.content.res.AssetManager";
    private static final String DEX_FILE_CLASS = "dalvik.system.DexFile";
    private static final String TOAST_WIDGET_CLASS = "android.widget.Toast";
    private static final String FRAGMENT_CLASS = "android.app.Fragment";

    private static final Class<?> contextClass;

    static {
        try {
            contextClass = Class.forName(CONTEXT_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static AppActivity getFragmentActivity(Object fragment){
        try {
            Class<?> fragmentClass = Class.forName(FRAGMENT_CLASS);
            Method getActivityMethod = fragmentClass.getMethod("getActivity");
            Object activity = getActivityMethod.invoke(fragment);
            return new AppActivity(activity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeToast(AppActivity appActivityContext, String text, boolean stayForLonger){
        int toastLength = stayForLonger ? 1 : 0;
        try {
            Class<?> toastWidgetClass = Class.forName(TOAST_WIDGET_CLASS);
            Method makeTextMethod = toastWidgetClass.getMethod("makeText", contextClass, CharSequence.class, int.class);
            Method showMethod = toastWidgetClass.getMethod("show");
            Object toast = makeTextMethod.invoke(null,appActivityContext.getObjectReference(),text,toastLength);
            showMethod.invoke(toast);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static Enumeration<String> getClassesInDexFile(File dexFile){
        try{
            Class<?> dexFileClass = Class.forName(DEX_FILE_CLASS);
            Constructor<?> cons = dexFileClass.getDeclaredConstructor(File.class);
            cons.setAccessible(true);
            Object dexFileObject = cons.newInstance(dexFile);
            Method getEntriesMethod = dexFileClass.getMethod("entries"); //DexFile.entries() -> returns Enumeration<String>
            return (Enumeration<String>) getEntriesMethod.invoke(dexFileObject);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected static Object dexClassLoaderInit(String path, ClassLoader classLoader){
        try{
            Class<?> dexClassLoaderClass = Class.forName(DEX_CLASS_LOADER_CLASS);
            Constructor<?> cons = dexClassLoaderClass.getDeclaredConstructor(String.class,String.class,String.class,ClassLoader.class);
            cons.setAccessible(true);
            return cons.newInstance(path,null,null,classLoader);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected static ClassLoader getClassLoader(AppActivity context){
        try {
            Method getClassLoaderMethod = contextClass.getMethod("getClassLoader");
            return (ClassLoader) getClassLoaderMethod.invoke(context.getObjectReference());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static Class<?> loadClass(Object dexClassLoaderInstance, String className){
        try{
            Class<?> dexClassLoaderClass = Class.forName(DEX_CLASS_LOADER_CLASS);
            Method loadClassMethod = dexClassLoaderClass.getMethod("loadClass", String.class);
            return (Class<?>) loadClassMethod.invoke(dexClassLoaderInstance, className);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Object getAssetManager(AppActivity appActivityContext) {
        try {
            Method getAssetsMethod = contextClass.getMethod("getAssets");
            return getAssetsMethod.invoke(appActivityContext.getObjectReference());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream openAsset(Object assetManager, String fileName) {
        try {
            Class<?> assetManagerClass = Class.forName(ASSET_MANAGER_CLASS);
            Method openMethod = assetManagerClass.getMethod("open", String.class);
            return (InputStream) openMethod.invoke(assetManager, fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the app's private external files directory (Android/data/com.saterskog.cell_lab/files/).
     * @param appActivityContext The Context object.
     * @return File object representing the directory.
     */
    protected static File getExternalFilesDir(AppActivity appActivityContext) {
        try {
            Method getExternalFilesDirMethod = contextClass.getMethod("getExternalFilesDir", String.class);
            return (File) getExternalFilesDirMethod.invoke(appActivityContext.getObjectReference(), (Object) null);  // null for default dir
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Lists all files in a directory.
     * @param dir The directory to list.
     * @return List of file names
     */
    protected static List<String> listFiles(File dir) {
        List<String> fileNames = new ArrayList<>();
        try {
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        fileNames.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fileNames;
    }

    /**
     * Reads the manifest YAML from a JAR file.
     * @param jarFile The JAR file to read.
     * @return Map of key-value pairs from the manifest, or empty map if failed.
     */
    protected static Map<String, String> readModManifest(File jarFile) {
        Map<String, String> manifestData = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry manifestEntry = jar.getEntry("mod_manifest.yml");
            if (manifestEntry != null) {
                try (InputStream inputStream = jar.getInputStream(manifestEntry);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        //Remove inline comments
                        int commentIndex = line.indexOf("#");
                        if (commentIndex > 0) {
                            line = line.substring(0, commentIndex).trim();
                        }
                        int colonIndex = line.indexOf(":");
                        if (colonIndex > 0) {
                            String key = line.substring(0, colonIndex).trim();
                            String value = line.substring(colonIndex + 1).trim();
                            manifestData.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return manifestData;
    }

}
