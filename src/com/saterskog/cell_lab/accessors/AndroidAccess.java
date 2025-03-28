package com.saterskog.cell_lab.accessors;

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

    public AndroidAccess(Object obj, Type type) {
        super(obj);
        this.accessType = type;
    }

    public Type getType(){
        return this.accessType;
    }

    // Reflection utilities for the Android API

    //Fully qualified class names of various android utils
    private static final String CONTEXT_CLASS = "android.content.Context";
    private static final String DEX_CLASS_LOADER_CLASS = "dalvik.system.DexClassLoader";
    private static final String ASSET_MANAGER_CLASS = "android.content.res.AssetManager";
    private static final String DEX_FILE_CLASS = "dalvik.system.DexFile";

    @SuppressWarnings("unchecked")
    public static Enumeration<String> getClassesInDexFile(File dexFile){
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

    public static Object dexClassLoaderInit(String path, ClassLoader classLoader){
        try{
            Class<?> dexClassLoaderClass = Class.forName(DEX_CLASS_LOADER_CLASS);
            Constructor<?> cons = dexClassLoaderClass.getDeclaredConstructor(String.class,String.class,String.class,ClassLoader.class);
            cons.setAccessible(true);
            return cons.newInstance(path,null,null,classLoader);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static ClassLoader getClassLoader(Object context){
        try {
            Class<?> contextClass = Class.forName(CONTEXT_CLASS);
            Method getClassLoaderMethod = contextClass.getMethod("getClassLoader");
            return (ClassLoader) getClassLoaderMethod.invoke(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> loadClass(Object dexClassLoaderInstance, String className){
        try{
            Class<?> dexClassLoaderClass = Class.forName(DEX_CLASS_LOADER_CLASS);
            Method loadClassMethod = dexClassLoaderClass.getMethod("loadClass", String.class);
            return (Class<?>) loadClassMethod.invoke(dexClassLoaderInstance, className);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Object getAssetManager(Object context) {
        try {
            Class<?> contextClass = Class.forName(CONTEXT_CLASS);
            Method getAssetsMethod = contextClass.getMethod("getAssets");
            return getAssetsMethod.invoke(context);
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
     * @param context The Context object.
     * @return File object representing the directory.
     */
    public static File getExternalFilesDir(Object context) {
        try {
            Class<?> contextClass = Class.forName(CONTEXT_CLASS);
            Method getExternalFilesDirMethod = contextClass.getMethod("getExternalFilesDir", String.class);
            return (File) getExternalFilesDirMethod.invoke(context, (Object) null);  // null for default dir
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Lists all files in a directory.
     * @param dir The directory to list.
     * @return List of file names
     */
    public static List<String> listFiles(File dir) {
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
    public static Map<String, String> readModManifest(File jarFile) {
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
