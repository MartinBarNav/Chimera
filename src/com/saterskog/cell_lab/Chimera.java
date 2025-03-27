package com.saterskog.cell_lab;

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
    public void updateFormatVersion(){
        ChimeraHooks.updateFormatVersion();
    }


    //Logging to logcat. For easy filtering on the adb shell, logs will be prefixed with a loggerID
    //by default: ChimeraLogger:"message"
    private static final String CHIMERA_LOGGER_ID = "ChimeraLogger:";

    public static void logMessage(String loggerID, String message){ System.out.println(loggerID + ": " + message); }
    public static void logMessage(String message){ logMessage(CHIMERA_LOGGER_ID,message); }
    public static void logError(String message){ System.err.println(CHIMERA_LOGGER_ID+message); }

}
