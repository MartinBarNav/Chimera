package com.saterskog.cell_lab;

import com.saterskog.cell_lab.accessors.GeneAccess;
import com.saterskog.cell_lab.accessors.GenomeEditorAccess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChimeraHooks {
    private static List<Object> mods = new ArrayList<>();
    private static Object genomeEditorView;
    private static boolean initialized=false;

    protected static void initMods(String[] classNames) {
        if(initialized) return;
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(ChimeraMod.class)) {
                    Object mod = clazz.getDeclaredConstructor().newInstance();
                    mods.add(mod);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initialized=true;
    }

    // In case a mod misbehaves or something, I'll just leave this here if it's ever needed.
    public static void unloadMod(Object mod){
        mods.remove(mod);
    }

    /*protected static void onGeneInstanceHook(Object gene){
        GeneAccess access = new GeneAccess(gene);
    }*/

    protected static void onCreateGeditorHook(Object geditorView, ArrayList<Object> controllers){
        genomeEditorView = geditorView;
        GenomeEditorAccess access = new GenomeEditorAccess(genomeEditorView, controllers);

        invokeModImplementationWithAccess("onCreateGenomeEditorViewHook",access);
    }

    private static <T> void invokeModImplementationWithAccess(String methodName,T access){
        // Invokes mod impl. with phony object to abstract away obfuscated methods and fields.
        // Works with static and non-static impl. and allows multiple mods to use the same hook.
        if (mods.isEmpty()) return;
        for (Object mod : mods) {
            try {
                Method method = mod.getClass().getMethod(methodName,access.getClass());
                try {
                    method.invoke(mod, access);
                }catch(IllegalArgumentException e){
                    //Try static implementation
                    method.invoke(access);
                } catch (IllegalAccessException e) {
                    System.err.println("Warning: Method: " + method.getName() + " on mod: " + mod.getClass().getName()
                            + "has valid name for hook but is inaccessible. Mod hooks must be public methods!");
                }
            } catch (NoSuchMethodException e) {
                //No implementation, ignore, keep searching.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Method getFirstModImplementation(String methodName, Class<?>... args){
        if (mods.isEmpty()) return null;

        for (Object mod : mods) {
            try {
                Method method = mod.getClass().getMethod(methodName,args);
                return method;
            } catch (NoSuchMethodException e) {
                //Mod doesn't implement method, should ignore and move on...
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Invoked from j.smali and can override vanilla behavior.
     * @param challenge an integer ID mapped to a given challenge in the challenge screen
     * @return false for vanilla behavior, true to unlock given challenge
     */
    protected static boolean unlockChallengeHook(int challenge){
        //In this case, the first mod to be loaded and invoked wins the implementation race.
        //So... uhh... at least it doesn't crash if two mods implement the same hook.
        Method method = getFirstModImplementation("unlockChallengeHook",int.class);

        if(method != null){
            try {
                return (boolean) method.invoke(challenge); //Only works with static methods
            }catch(IllegalArgumentException e){
                //If the method isn't static, then simply ignore it. To avoid crashing over essentially a nothingburger.
                System.err.println("Non-fatal error: method unlockChallengeHook() must be static!");
                return false;
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return false; //Vanilla behavior
    }

    // Reflection utilities

    public static void invokeMethodNoParams(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.invoke(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName,paramTypes);
            method.invoke(target,args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This traces the stack to make sure a given method at frameDepth+1 is either a static block <clinit> or constructor <init>
    // .skip(frameDepth) skips the specified stack frames. Should generally be 1 but in cases of more complex stack traces it could be larger.
    // .limit(1) should limit our search to the immediate caller after the skips.
    // frameDepth+1 is therefore the location (in frames traced) where the method we are checking has it's frame.
    // For more info on stack frames, read: https://www.geeksforgeeks.org/stack-frame-in-computer-organization/
    public static boolean isCallerInitializer(int frameDepth) {
        return StackWalker.getInstance().walk(frames ->
                frames.skip(frameDepth).limit(1)
                        .anyMatch(f -> f.getMethodName().equals("<clinit>") || f.getMethodName().equals("<init>")));
    }
}
