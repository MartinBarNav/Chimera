package com.saterskog.cell_lab;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChimeraHooks {
    private static List<Object> mods = new ArrayList<>();

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

    /**
     * Invoked from j.smali and can override vanilla behavior.
     * @param challenge an integer ID mapped to a given challenge in the challenge screen
     * @return false for vanilla behavior, true to unlock given challenge
     */
    public static boolean unlockChallengeHook(int challenge){
        if (mods.isEmpty()) return false;

        for (Object mod : mods) {
            try {
                Method method = mod.getClass().getMethod("unlockChallengeHook",int.class);
                try {
                    boolean result = (boolean) method.invoke(mod, challenge);
                    if (result) return result; // First valid override wins
                }catch (IllegalArgumentException e){
                    // This will handle static mod methods
                    boolean result = (boolean) method.invoke(challenge);
                    if (result) return result;
                }
            } catch (NoSuchMethodException e) {
                // Mod doesn’t implement this hook, skip
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //false = Vanilla fallback
        return false;
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
