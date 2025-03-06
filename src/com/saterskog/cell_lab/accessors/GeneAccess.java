package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GeneAccess extends Accessor{
    private float[] rgbColor,floatProperties;
    public static int fPropertiesCount=ChimeraHooks.DEFAULT_FPROPERTY_COUNT; //This controls the size of the float properties array. 8 by default, mods can add more.
    private static Field floatPropertiesMaxField,floatPropertiesMinField;
    private static ArrayList<GeneProperty<Float>> modFloatProperties;
    private Field floatPropertiesField,rgbColorField;

    public GeneAccess(Object gene, AndroidAccess parcel) throws RuntimeException {
        this(gene);
        setParcel(parcel);
    }

    public GeneAccess(Object gene, ObjectInputStream stream) {
        this(gene);
        setInStream(stream);
    }

    public GeneAccess(Object gene, ObjectOutputStream stream) {
        this(gene);
        setOutStream(stream);
    }

    public GeneAccess(Object gene){
        super(gene);

        try{
            this.rgbColorField = Class.forName("com.saterskog.cell_lab.Gene").getField("a");
            this.floatPropertiesField = Class.forName("com.saterskog.cell_lab.Gene").getField("v");

            this.rgbColor = (float[]) rgbColorField.get(this.getObjectReference());
            this.floatProperties = (float[]) floatPropertiesField.get(this.getObjectReference());
        }catch(Exception e){
            throw new RuntimeException();
        }
    }


    public static void init(){
        modFloatProperties = new ArrayList<GeneProperty<Float>>();
    }

    // This method should only be called after all the mods have been initialized and thus had time to call
    // requestAdditionalGeneFloatProperties(). Only then can we load the gene class.
    // called from: ChimeraHooks.initMods() after all mod constructors were invoked.
    public static void loadStatic(){
        //It's important to NOT load the Gene class before this, otherwise the static block will run and fPropertiesCount will be 8 (the default)
        //and since the fields are final, it cannot be changed later.
        try {
            floatPropertiesMaxField = Class.forName("com.saterskog.cell_lab.Gene").getField("A");
            floatPropertiesMinField = Class.forName("com.saterskog.cell_lab.Gene").getField("z");

            if(modFloatProperties != null) {
                int j = 0;
                for (int i = ChimeraHooks.DEFAULT_FPROPERTY_COUNT; i < fPropertiesCount; i++) {
                    ((float[]) floatPropertiesMaxField.get(null))[i] = modFloatProperties.get(j).getMaximumValue();
                    ((float[]) floatPropertiesMinField.get(null))[i] = modFloatProperties.get(j).getMinimumValue();
                    j++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setMinimumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMinimumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.DEFAULT_FPROPERTY_COUNT).setMinimumValue((float) value);
            }
        }

    }

    public static <T> void setMaximumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMaximumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.DEFAULT_FPROPERTY_COUNT).setMaximumValue((float) value);
            }
        }
    }

    /** This method should only be called on mod initialization, either inside a static block or the mod constructor.
     * Failure to do so will result in the offending mod getting unloaded.
     */
    public static GeneProperty<Float>[] requestAdditionalGeneFloatProperties(Object mod, int amount){
        if(!ChimeraHooks.isCallerInitializer(1)){
            // This is to prevent unpredictable behavior if mods are allowed to dynamically change the sizes of arrays in different gene modes.
            System.err.println("Request for additional float properties denied! Request must be made during mod initialization!"
                    + " culprit: " + mod.getClass().getSimpleName());
            ChimeraHooks.unloadMod(mod); //to prevent a crash
        }

        GeneProperty<Float>[] fp = new GeneProperty[amount];
        for(int i=0;i<amount;i++){
            fp[i] = new GeneProperty<>(fPropertiesCount+i);
            fp[i].setMaximumValue(1.f); //default, in case modder doesn't set it
            fp[i].setMinimumValue(0.f); //default
            modFloatProperties.add(fp[i]);
        }
        fPropertiesCount += amount;

        return fp;
    }

    //TODO: make safety checks. This code is currently very unsafe.

    public void savePropertiesToParcel(GeneProperty<Float>[] properties){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was savePropertiesToParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<Float> property : properties) {
            ChimeraHooks.invokeMethod(this.getParcel().getObjectReference(), "writeFloat", new Class[]{float.class},
                    this.floatProperties[property.getIndex()]);
        }
    }

    public void loadPropertiesFromParcel(GeneProperty<Float>[] properties){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was loadPropertiesFromParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<Float> property : properties) {
            this.floatProperties[property.getIndex()] = (float) ChimeraHooks.invokeMethodNoParams(this.getParcel().getObjectReference(), "readFloat");
        }
    }

    public void savePropertiesToStream(GeneProperty<Float>[] properties){
        if(this.getOutStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was savePropertiesToStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<Float> property : properties){
            ChimeraHooks.invokeMethod(this.getOutStream(),"writeFloat", new Class[]{float.class},
                    this.floatProperties[property.getIndex()]);
        }
    }

    public void loadPropertiesFromStream(GeneProperty<Float>[] properties){
        if(this.getInStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was loadPropertiesFromStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<Float> property : properties){
            this.floatProperties[property.getIndex()] = (float) ChimeraHooks.invokeMethodNoParams(this.getInStream(),"readFloat");
        }
    }

}
