package com.saterskog.cell_lab;

import java.lang.reflect.Method;
import java.util.*;

/** This is the class mods will be interfacing with to interact with ChimeraAPI and it's utilities.
*/
public class Chimera {
    enum CellLabClass{
        CELL,
        GENE,
        CELL_WORLD
    }

    // ChimeraHooks also contains this method. This is redundant but may be good for readability
    public static int getCurrentFormatVersion(){
        return ChimeraHooks.modFormatVersion;
    }
    public static void updateFormatVersion(){ChimeraHooks.modFormatVersion++;}

    //Logging to logcat. For easy filtering on the adb shell, logs will be prefixed with a loggerID
    //by default: ChimeraLogger:"message"
    private static final String CHIMERA_LOGGER_ID = "ChimeraLogger";

    public static void logMessage(String loggerID, String message){ System.out.println(loggerID + ": " + message); }
    public static void logMessage(String message){ logMessage(CHIMERA_LOGGER_ID,message); }
    public static void logError(String message){ System.err.println(CHIMERA_LOGGER_ID+": "+message); }
    public static void logException(Exception e){logError(e.getMessage() +"\n"+e.getCause());}


    //Reflection utilities

    public static Object invokeMethodNoParams(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            Chimera.logException(e);
        }
        return null;
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName,paramTypes);
            return method.invoke(target,args);
        } catch (Exception e) {
            Chimera.logException(e);
        }

        return null;
    }

    //This method traces the stack to make sure that the caller at [frameDepth] (skipping this method's frame) is a constructor or static block
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
