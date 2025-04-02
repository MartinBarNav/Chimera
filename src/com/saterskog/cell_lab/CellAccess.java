package com.saterskog.cell_lab;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

public class CellAccess extends Accessor{

    public static int modesCount= ChimeraHooks.VANILLA_MODES_COUNT;
    public int formatVersion=ChimeraHooks.VANILLA_VERSION;
    // Cell Lab uses a double buffer (signal count*2) for (diffusion?) calculations.
    public static int signalCount=ChimeraHooks.VANILLA_SIGNAL_COUNT, doubledSignalCount=ChimeraHooks.VANILLA_SIGNAL_COUNT*2,
    secreteSignalStartIndex=11,secretables=ChimeraHooks.VANILLA_SECRETEABLE_CHEMICALS,secreteArrayLength=secretables+doubledSignalCount;

    //Fields
    private static Field modesFieldPointer;

    // Class names
    private static final String cellClassName = "com.saterskog.cell_lab.Cell";

    public CellAccess(Object obj, ObjectInputStream stream, int vers){
        this(obj);
        setInStream(stream);
        this.formatVersion = vers;
    }

    public CellAccess(Object obj, ObjectOutputStream stream){
        this(obj);
        setOutStream(stream);
    }

    protected CellAccess(Object obj) {
        super(obj);
    }

    public static void loadStatic(){
        doubledSignalCount=signalCount*2;
        int newSignals = signalCount-ChimeraHooks.VANILLA_SIGNAL_COUNT;
        secreteSignalStartIndex=11+newSignals; //Start index of the +S# item in secrocyte droplist.
        secreteArrayLength=secretables+doubledSignalCount;

        try {
            Class<?> cellClass = Class.forName("com.saterskog.cell_lab.Cell");
            modesFieldPointer = cellClass.getField("I");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public int getFormatVersion(){return this.formatVersion;}

    /**
     * Requests additional cell modes for the mod. This method ensures that the request
     * is made only from a valid constructor or static block belonging to the main mod class.
     * otherwise, the changes could not be made on time.
     *
     * <p>Each requested mode is assigned an index, for internal use.</p>
     *
     * @param amount The number of additional cell modes.
     * @param caller The object making the request; must be a constructor or static block
     *               of a main mod class.
     * @return An array of {@link CellProperty} instances representing the newly allocated modes.
     */
    public static CellProperty[] requestAdditionalModes(int amount, Object caller){
        if(!Chimera.validateRequest(1, caller)){
            throw new RuntimeException("Request for additional modes DENIED!" + " caller must be constructor or static block" +
                    " belonging to the main mod class!\nculprit: "+ caller.getClass());
        }

        CellProperty[] extraModes = new CellProperty[amount];
        for(int i=0;i<amount;i++){
            extraModes[i] = new CellProperty(modesCount, CellProperty.Type.CELL_MODE);
            modesCount++;
        }
        return extraModes;
    }

    /**
     * Requests additional cell signals for the mod. This method ensures that the request
     * is made only from a valid constructor or static block belonging to a main mod class.
     *
     * <p>Each requested signal is assigned an index, for internal use.</p>
     *
     * @param amount The number of additional cell signals.
     * @param caller The object making the request; must be a constructor or static block
     *               of a main mod class.
     * @return An array of {@link CellProperty} instances representing the newly allocated signals.
     */
    public static CellProperty[] requestAdditionalSignals(int amount, Object caller){
        if(!Chimera.validateRequest(1, caller)){
            throw new RuntimeException("Request for additional signals DENIED!" + " caller must be constructor or static block" +
                    " belonging to the main mod class!\nculprit: "+ caller.getClass());
        }

        CellProperty[] extraSignals = new CellProperty[amount];
        for(int i=0;i<amount;i++){
            extraSignals[i] = new CellProperty(signalCount, CellProperty.Type.CELL_SIGNAL);
            signalCount++;
        }
        return extraSignals;
    }

}
