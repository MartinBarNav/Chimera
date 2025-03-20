package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;
import com.saterskog.cell_lab.ChimeraMod;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GeneAccess extends Accessor{
    private float[] rgbColor,floatProperties;
    private int[] intProperties;
    public static int fPropertiesCount=ChimeraHooks.VANILLA_FPROPERTY_COUNT,intPropertiesCount=ChimeraHooks.VANILLA_IPROPERTY_COUNT;
    private static ArrayList<GeneProperty<Float>> modFloatProperties;
    private static ArrayList<GeneProperty<Integer>> modIntProperties;
    private int formatVersion;

    public GeneAccess(Object gene, AndroidAccess parcel) throws RuntimeException {
        this(gene);
        setParcel(parcel);
    }

    public GeneAccess(Object gene, ObjectInputStream stream, int version) {
        this(gene);
        this.formatVersion = version;
        setInStream(stream);
    }

    public GeneAccess(Object gene, ObjectOutputStream stream) {
        this(gene);
        setOutStream(stream);
    }

    public GeneAccess(Object gene){
        super(gene);

        try{
            Field rgbColorFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("a");
            Field floatPropertiesFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("v");
            Field intPropertiesFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("u");

            this.rgbColor = (float[]) rgbColorFieldPointer.get(this.getObjectReference());
            this.floatProperties = (float[]) floatPropertiesFieldPointer.get(this.getObjectReference());
            this.intProperties = (int[]) intPropertiesFieldPointer.get(this.getObjectReference());
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
            Field floatPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("A");
            Field floatPropertiesMinFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("z");
            Field intPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("w");

            int j = 0;
            for (int i = ChimeraHooks.VANILLA_FPROPERTY_COUNT; i < fPropertiesCount; i++) {
                ((float[]) floatPropertiesMaxFieldPointer.get(null))[i] = modFloatProperties.get(j).getMaximumValue();
                ((float[]) floatPropertiesMinFieldPointer.get(null))[i] = modFloatProperties.get(j).getMinimumValue();
                j++;
            }

            j = 0;
            for (int i = ChimeraHooks.VANILLA_IPROPERTY_COUNT; i < intPropertiesCount; i++) {
                ((int[]) intPropertiesMaxFieldPointer.get(null))[i] = modIntProperties.get(j).getMaximumValue();
                j++;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getFormatVersion(){
        return this.formatVersion;
    }

    public static <T extends Number> void setMinimumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMinimumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.VANILLA_FPROPERTY_COUNT).setMinimumValue((float) value);
            }
        }

    }

    public Number getValueOf(GeneProperty<? extends Number> property){
        if(property.type == int.class){
            return intProperties[property.getIndex()];
        }
        else if(property.type == float.class){
            return floatProperties[property.getIndex()];
        }
        return null;
    }

    public static <T extends Number> void setMaximumValueOfProperty(GeneProperty<T> property, T value){
        if(ChimeraHooks.isCallerInitializer(1)) {
            property.setMaximumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.VANILLA_FPROPERTY_COUNT).setMaximumValue((float) value);
            }
            else if(value instanceof  Integer){
                modIntProperties.get(property.getIndex()-ChimeraHooks.VANILLA_IPROPERTY_COUNT).setMaximumValue((int) value);
            }
        }
    }

    /** This method should only be called on mod initialization, either inside a static block or the mod constructor.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> GeneProperty<T>[] requestAdditionalGeneProperties(int amount, Class<T> type, Object mod) {
        if (!ChimeraHooks.isCallerInitializer(1)) {
            throw new RuntimeException("Request for additional" + type.getSimpleName().toLowerCase() +
                    "properties denied! Request must be made during mod initialization!");
        }
        if(!mod.getClass().isAnnotationPresent(ChimeraMod.class)){
            throw new RuntimeException("Request for additional properties denied! Invalid mod reference.");
        }

        GeneProperty<T>[] properties = new GeneProperty[amount];

        for (int i = 0; i < amount; i++) {
            if (type == float.class) {
                properties[i] = new GeneProperty<>(fPropertiesCount + i);
                properties[i].type = float.class;
                properties[i].setMaximumValue((T) Float.valueOf(1.f));
                properties[i].setMinimumValue((T) Float.valueOf(0.f));
                properties[i].setMod(mod);
                modFloatProperties.add((GeneProperty<Float>) properties[i]);
            } else if (type == int.class) {
                properties[i] = new GeneProperty<>(intPropertiesCount + i);
                properties[i].type = int.class;
                properties[i].setMaximumValue((T) Integer.valueOf(1));
                properties[i].setMod(mod);
                modIntProperties.add((GeneProperty<Integer>) properties[i]);
            }
        }

        if (type == float.class) {
            fPropertiesCount += amount;
        } else if (type == int.class) {
            intPropertiesCount += amount;
        }

        ChimeraHooks.modFormatVersion++;

        return properties;
    }

    //TODO: make safety checks. This code is currently very unsafe.

    public <T extends Number> void savePropertiesToParcel(GeneProperty<T>[] properties){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was savePropertiesToParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<T> property : properties) {
            if(property.type == float.class) {
                ChimeraHooks.invokeMethod(this.getParcel().getObjectReference(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(property.type == int.class){
                ChimeraHooks.invokeMethod(this.getParcel().getObjectReference(), "writeInt", new Class[]{int.class},
                        this.intProperties[property.getIndex()]);
            }
        }
    }

    public <T extends Number> void loadPropertiesFromParcel(GeneProperty<T>[] properties){
        if(this.getParcel() == null){
            System.err.println("Gene access does not contain a parcel reference! Was loadPropertiesFromParcel() called outside" +
                    " the scope of a valid hook?");
            return;
        }
        for(GeneProperty<T> property : properties) {
            if (property.type== float.class) {
                this.floatProperties[property.getIndex()] = (float) ChimeraHooks.invokeMethodNoParams(this.getParcel().getObjectReference(), "readFloat");
            }
            else if(property.type == int.class){
                this.intProperties[property.getIndex()] = (int) ChimeraHooks.invokeMethodNoParams(this.getParcel().getObjectReference(), "readInt");
            }
        }
    }

    public <T extends Number> void savePropertiesToStream(GeneProperty<T>[] properties){
        if(this.getOutStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was savePropertiesToStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<T> property : properties){
            if(property.type == float.class) {
                ChimeraHooks.invokeMethod(this.getOutStream(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(property.type == int.class){
                ChimeraHooks.invokeMethod(this.getOutStream(), "writeInt", new Class[]{int.class},
                        this.intProperties[property.getIndex()]);
            }
        }
    }

    public <T extends Number> void loadPropertiesFromStream(GeneProperty<T>[] properties){
        if(this.getInStream() == null){
            System.err.println("Gene access does not contain a stream reference! Was loadPropertiesFromStream() called outside" +
                    " the scope of a valid hook?");
            return;
        }

        for(GeneProperty<T> property : properties){
            try {
                if(property.type == float.class) {
                    this.floatProperties[property.getIndex()] = this.getInStream().readFloat();
                }
                else if(property.type == int.class){
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
