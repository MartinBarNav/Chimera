package com.saterskog.cell_lab;

import com.saterskog.cell_lab.accessors.Accessor;
import com.saterskog.cell_lab.accessors.AndroidAccess;
import com.saterskog.cell_lab.accessors.GeneAccess;
import com.saterskog.cell_lab.accessors.GenomeEditorAccess;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChimeraHooks {
    private static final List<Object> mods = new ArrayList<>();
    private static boolean initialized=false;
    public static final int VANILLA_FPROPERTY_COUNT=7,VANILLA_IPROPERTY_COUNT=11,VANILLA_MODES_COUNT=40; //vanilla value
    public static final int VANILLA_VERSION = 95;

    protected static void initMods(String[] classNames) {
        if(initialized) return;
        GeneAccess.init();
        GenomeEditorAccess.init();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(ChimeraMod.class)) {
                    Constructor<?> cons = clazz.getDeclaredConstructor();
                    cons.setAccessible(true);
                    Object mod = cons.newInstance();
                    mods.add(mod);
                }
            } catch (Exception e) {
                System.err.println(e.getCause());
            }
        }

        GeneAccess.loadStatic();
        initialized=true;
    }

    // In case a mod misbehaves
    public static void unloadMod(Object mod){
        mods.remove(mod);
    }

    protected static void loadGeneFromStream(Object gene, ObjectInputStream stream){
        GeneAccess access = new GeneAccess(gene, stream);
        try {
            invokeModImplementationWithAccess("onLoadGeneFromStream", access);
        } catch (Exception e) {
            System.err.println("Error while reading gene stream. Possible attempt to read non-existant" +
                    " custom property?");
        }

    }

    protected static void saveGeneToStream(Object gene, ObjectOutputStream stream){
        GeneAccess access = new GeneAccess(gene, stream);
        invokeModImplementationWithAccess("onSaveGeneToStream",access);
    }

    protected static void loadGeneFromParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);
        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        try {
            invokeModImplementationWithAccess("onLoadGeneFromParcel", access);
        } catch (Exception e) {
            System.err.println("Error while reading gene parcel. Possible attempt to read non-existant" +
                    " custom property?");
        }
    }

    protected static void saveGeneToParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);

        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        invokeModImplementationWithAccess("onSaveGeneToParcel",access);
    }

    protected static void onCreateGeditorHook(Object geditorView, ArrayList<Object> controllers){
        GenomeEditorAccess access = new GenomeEditorAccess(geditorView, controllers);

        invokeModImplementationWithAccess("onCreateGenomeEditorViewHook",access);
    }


    /**
     * Invoked from j.smali and can override vanilla behavior.
     * @param challenge an integer ID mapped to a given challenge in the challenge screen
     * @return false for vanilla behavior, true to unlock given challenge
     */
    protected static boolean unlockChallengeHook(int challenge){
        //In this case, the first mod to be loaded and invoked wins the implementation race.
        Object ret = invokeFirstImplementation("unlockChallengeHook", new Class[]{int.class}, challenge);
        if(ret != null){
            return (boolean) ret;
        }
        return false; //Vanilla behavior
    }

    // Reflection utilities


    private static void invokeModImplementation(String methodName, Object... args) {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i=0;i<args.length;i++){
            paramTypes[i] = args.getClass();
        }
        invokeAllModImplementations(methodName, paramTypes, args);
    }

    private static <T extends Accessor> void invokeModImplementationWithAccess(String methodName, T access) {
        invokeAllModImplementations(methodName, new Class<?>[]{access.getClass()}, access);
    }

    private static void invokeAllModImplementations(String methodName, Class<?>[] paramTypes, Object... args) {
        if (mods.isEmpty()) return;

        for (Object mod : mods) {
            try {
                Method method = mod.getClass().getMethod(methodName, paramTypes);
                try {
                    method.invoke(mod, args);
                } catch (IllegalArgumentException e) {
                    // Try static implementation
                    method.invoke(args);
                } catch (IllegalAccessException e) {
                    System.err.println("Warning: Method: " + method.getName() + " on mod: " + mod.getClass().getName()
                            + " has valid name for hook but is inaccessible. Mod hooks must be public methods!");
                }
            } catch (NoSuchMethodException e) {
                // No implementation, ignore, keep searching.
            } catch (Exception e) {
                unloadMod(mod);
                e.printStackTrace();
            }
        }
    }

    private static Object invokeFirstImplementation(String methodName, Class<?>[] paramTypes, Object... args){
        if (mods.isEmpty()) return null;

        for (Object mod : mods) {
            try {
                Method method = mod.getClass().getMethod(methodName,paramTypes);
                try {
                    return method.invoke(mod, args);
                } catch (IllegalArgumentException e){
                    return method.invoke(args);
                }
            }
            catch (NoSuchMethodException e) {
                //Mod doesn't implement method, should ignore and move on...
            } catch (Exception e) {
                unloadMod(mod);
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object invokeMethodNoParams(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object ret = method.invoke(target);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName,paramTypes);
            Object ret = method.invoke(target,args);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //This method traces the stack to make sure that the caller at [frameDepth] (skipping this method's frame) is a constructor or static block
    // For more info on stack frames, read: https://www.geeksforgeeks.org/stack-frame-in-computer-organization/
    public static boolean isCallerInitializer(int frameDepth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace(); //array of stack frames

        //stack snapshot apparently taken at getStackTrace()
        //I tried with +2 but it doesn't work. I still have no idea why there's an extra frame, but whatever, it works.
        int targetIndex = frameDepth + 3;

        StackTraceElement frame = stackTrace[targetIndex];
        String methodName = frame.getMethodName();
        return "<clinit>".equals(methodName) || "<init>".equals(methodName);
    }
}
