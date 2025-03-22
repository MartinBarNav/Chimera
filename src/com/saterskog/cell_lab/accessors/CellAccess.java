package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;
import com.saterskog.cell_lab.ChimeraMod;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

public class CellAccess extends Accessor{
    private static Field modesFieldPointer;
    public static int modesCount= ChimeraHooks.VANILLA_MODES_COUNT;
    public int formatVersion=ChimeraHooks.VANILLA_VERSION;
    // Cell Lab uses a double buffer (signal count*2) for (diffusion?) calculations.
    public static int signalCount=ChimeraHooks.VANILLA_SIGNAL_COUNT, doubledSignalCount=ChimeraHooks.VANILLA_SIGNAL_COUNT*2,
    secreteSignalStartIndex=11,secretables=ChimeraHooks.VANILLA_SECRETEABLE_CHEMICALS,secreteArrayLength=secretables+doubledSignalCount;

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
            modesFieldPointer = Class.forName("com.saterskog.cell_lab.Cell").getField("I");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public int getFormatVersion(){return this.formatVersion;}

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
            extraModes[i].setMod(mod);
            modesCount++;
        }

        ChimeraHooks.modFormatVersion++;
        return extraModes;
    }

}
