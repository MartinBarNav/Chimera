package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;
import com.saterskog.cell_lab.ChimeraMod;

import java.lang.reflect.Field;

public class CellAccess extends Accessor{
    private static Field modesFieldPointer;
    public static int modesCount= ChimeraHooks.VANILLA_MODES_COUNT;
    public static int formatVersion=ChimeraHooks.VANILLA_VERSION;
    public static int signalCount=ChimeraHooks.VANILLA_SIGNAL_COUNT;

    protected CellAccess(Object obj) {
        super(obj);
    }

    public static void loadStatic(){
        try {
            modesFieldPointer = Class.forName("com.saterskog.cell_lab.Cell").getField("I");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static int getCurrentFormatVersion(){
        return formatVersion;
    }

    public static CellProperty[] requestAdditionalModes(int amount, Object mod){
        if (!ChimeraHooks.isCallerInitializer(1)){
            throw new RuntimeException("Request for additional cell modes denied!"
                    + " Request must be made during mod initialization!");
        }
        if(!mod.getClass().isAnnotationPresent(ChimeraMod.class)){
            throw new RuntimeException("Request for additional cell modes denied! Invalid mod reference.");
        }

        CellProperty[] extraModes = new CellProperty[amount];
        for(int i=0;i<amount;i++){
            extraModes[i] = new CellProperty(modesCount);
            modesCount++;
        }

        formatVersion++;
        return extraModes;
    }

}
