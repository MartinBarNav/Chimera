package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GeneAccess extends Accessor{
    private float[] rgbColor,floatProperties;
    private int[] intProperties;
    public static int fPropertiesCount=ChimeraHooks.DEFAULT_FPROPERTY_COUNT,intPropertiesCount=ChimeraHooks.DEFAULT_IPROPERTY_COUNT;
    private static Field floatPropertiesMaxField,floatPropertiesMinField, intPropertiesMaxField;
    private static ArrayList<GeneProperty<Float>> modFloatProperties;
    private static ArrayList<GeneProperty<Integer>> modIntProperties;
    private Field floatPropertiesField,intPropertiesField,rgbColorField;

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
            this.intPropertiesField = Class.forName("com.saterskog.cell_lab.Gene").getField("u");

            this.rgbColor = (float[]) rgbColorField.get(this.getObjectReference());
            this.floatProperties = (float[]) floatPropertiesField.get(this.getObjectReference());
            this.intProperties = (int[]) intPropertiesField.get(this.getObjectReference());
        }catch(Exception e){
            throw new RuntimeException();
        }
    }


    public static void init(){
        modFloatProperties = new ArrayList<>();
        modIntProperties = new ArrayList<>();
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
            intPropertiesMaxField = Class.forName("com.saterskog.cell_lab.Gene").getField("w");

            int j = 0;
            for (int i = ChimeraHooks.DEFAULT_FPROPERTY_COUNT; i < fPropertiesCount; i++) {
                ((float[]) floatPropertiesMaxField.get(null))[i] = modFloatProperties.get(j).getMaximumValue();
                ((float[]) floatPropertiesMinField.get(null))[i] = modFloatProperties.get(j).getMinimumValue();
                j++;
            }

            j = 0;
            for (int i = ChimeraHooks.DEFAULT_IPROPERTY_COUNT; i < intPropertiesCount; i++) {
                ((float[]) intPropertiesMaxField.get(null))[i] = modIntProperties.get(j).getMaximumValue();
                j++;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Number> void setMinimumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMinimumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.DEFAULT_FPROPERTY_COUNT).setMinimumValue((float) value);
            }
        }

    }

    public static <T extends Number> void setMaximumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMaximumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.DEFAULT_FPROPERTY_COUNT).setMaximumValue((float) value);
            }
            else if(value instanceof  Integer){
                modIntProperties.get(property.getIndex()-ChimeraHooks.DEFAULT_IPROPERTY_COUNT).setMaximumValue((int) value);
            }
        }
    }

    /** This method should only be called on mod initialization, either inside a static block or the mod constructor.
     * Failure to do so will result in the offending mod getting unloaded.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> GeneProperty<T>[] requestAdditionalGeneProperties(int amount, Class<T> type) {
        if (!ChimeraHooks.isCallerInitializer(1)) {
            System.err.println("Request for additional " + type.getSimpleName().toLowerCase()
                    + " properties denied! Request must be made during mod initialization!");
            return new GeneProperty[0];
        }

        GeneProperty<T>[] properties = new GeneProperty[amount];

        for (int i = 0; i < amount; i++) {
            if (type == float.class) {
                properties[i] = new GeneProperty<>(fPropertiesCount + i);
                properties[i].setMaximumValue((T) Float.valueOf(1.f));
                properties[i].setMinimumValue((T) Float.valueOf(0.f));
                modFloatProperties.add((GeneProperty<Float>) properties[i]);
            } else if (type == int.class) {
                properties[i] = new GeneProperty<>(intPropertiesCount + i);
                properties[i].setMaximumValue((T) Integer.valueOf(1));
                modIntProperties.add((GeneProperty<Integer>) properties[i]);
            }
        }

        if (type == float.class) {
            fPropertiesCount += amount;
        } else if (type == int.class) {
            intPropertiesCount += amount;
        }

        return properties;
    }

    //TODO: make safety checks. This code is currently very unsafe.

    public <T extends Number> void savePropertiesToParcel(GeneProperty<T>[] properties, Class<T> type){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was savePropertiesToParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<T> property : properties) {
            if(type == float.class) {
                ChimeraHooks.invokeMethod(this.getParcel().getObjectReference(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(type == int.class){
                ChimeraHooks.invokeMethod(this.getParcel().getObjectReference(), "writeInt", new Class[]{int.class},
                        this.intProperties[property.getIndex()]);
            }
        }
    }

    public <T extends Number> void loadPropertiesFromParcel(GeneProperty<T>[] properties, Class<T> type){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was loadPropertiesFromParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<T> property : properties) {
            if (type == float.class) {
                this.floatProperties[property.getIndex()] = (float) ChimeraHooks.invokeMethodNoParams(this.getParcel().getObjectReference(), "readFloat");
            }
            else if(type == int.class){
                this.intProperties[property.getIndex()] = (int) ChimeraHooks.invokeMethodNoParams(this.getParcel().getObjectReference(), "readInt");
            }
        }
    }

    public <T extends Number> void savePropertiesToStream(GeneProperty<T>[] properties, Class<T> type){
        if(this.getOutStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was savePropertiesToStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<T> property : properties){
            if(type == float.class) {
                ChimeraHooks.invokeMethod(this.getOutStream(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(type == int.class){
                ChimeraHooks.invokeMethod(this.getOutStream(), "writeInt", new Class[]{int.class},
                        this.intProperties[property.getIndex()]);
            }
        }
    }

    public <T extends Number> void loadPropertiesFromStream(GeneProperty<T>[] properties, Class<T> type){
        if(this.getInStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was loadPropertiesFromStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<T> property : properties){
            try {
                if(type == float.class) {
                    this.floatProperties[property.getIndex()] = this.getInStream().readFloat();
                }
                else if(type == int.class){
                    this.intProperties[property.getIndex()] = this.getInStream().readInt();
                }
            } catch (EOFException e) {
                System.err.println("Mod "+property.getMod().getClass().getSimpleName() + " attempted to read non-existent property." +
                        "Handled gracefully for vanilla compatibility.");
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }
}
