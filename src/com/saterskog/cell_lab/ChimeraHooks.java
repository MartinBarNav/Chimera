package com.saterskog.cell_lab;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChimeraHooks {
    private static List<Object> mods = new ArrayList<>();

    private static Object genomeEditorView;

    public static void initMods(String[] classNames) {
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
    }

    public static void onCreateGeditorHook(Object geditorView, ArrayList<Object> controllers){
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
                    System.err.println("Method: " + method.getName() + " on mod: " + mod.getClass().getName()
                            + " is inaccessible. Mod hooks must be public methods!");
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
    public static boolean unlockChallengeHook(int challenge){
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
}
