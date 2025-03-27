package com.saterskog.cell_lab.accessors;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

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

    private static Object getAssetManager(Object appActivity) {
        try {
            Class<?> contextClass = Class.forName(CONTEXT_CLASS);
            Method getAssetsMethod = contextClass.getMethod("getAssets");
            return getAssetsMethod.invoke(appActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static InputStream openAsset(Object assetManager, String fileName) {
        try {
            Class<?> assetManagerClass = Class.forName(ASSET_MANAGER_CLASS);
            Method openMethod = assetManagerClass.getMethod("open", String.class);
            return (InputStream) openMethod.invoke(assetManager, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads an asset text file and returns its contents as a String array, split by newlines.
     * @param appActivity The app activity object reference
     * @param fileName Name of the asset file (e.g., "mods.txt").
     * @return Array of strings (lines from the file), or empty array if failed.
     */
    public static String[] readAssetTextFile(Object appActivity, String fileName) {
        try {
            Object assetManager = getAssetManager(appActivity);
            if (assetManager == null) {
                throw new RuntimeException("Failed to get AssetManager");
            }


            InputStream inputStream = openAsset(assetManager, fileName);
            if (inputStream == null) {
                throw new RuntimeException("Failed to open asset file: " + fileName);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];  // 1024 bytes buffer, matching smali
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            String content = byteArrayOutputStream.toString();
            return content.split("\n");
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
