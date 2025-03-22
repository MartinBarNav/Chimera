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
    public static int fPropertiesCount=ChimeraHooks.VANILLA_FPROPERTY_COUNT,intPropertiesCount=ChimeraHooks.VANILLA_IPROPERTY_COUNT;
    private static ArrayList<GeneProperty<Float>> modFloatProperties;
    private static ArrayList<GeneProperty<Integer>> modIntProperties;
    private int formatVersion;

    public enum StaticArray{
        INTMAX,
        FMIN,
        FMAX
    }
    /**
     * Represents a pending modification to a static array, specifying the index at which a new value should be inserted.
     * These modifications are collected in a list (or queue) and applied all at once during class loading to patch the vanilla static field contents.
     *
     * @param val The type of value to be inserted, subclass of {@link Number}.
     * @param type an entry in the StaticArray enum mapped to the target static array
     */
    public record QueuedStaticChange(int index, Number val, StaticArray type) {}
    private static ArrayList<QueuedStaticChange> queuedChanges;

    //Cell lab field references
    private float[] rgbColor,floatProperties; //a[4],v[7]
    private static float[] floatPropertiesMin, floatPropertiesMax; //z[7],A[7]
    /* Vanilla indices (float properties):
     *  0 = sense output type
     *  1 = red (smell)
     *  2 = green (smell)
     *  3 = blue (smell)
     *  4 = color sense threshold
     *  5 = adhesin length
     *  6 = Cytoskeleton
     * */

    private int[] intProperties;//u[11]
    private static int[] intPropertiesMax; //w[11]
    /* Vanilla indices:
     *  0 = virus copy from (mode number)
     *  1 = gamete compatibility (mode number)
     *  2 = sense output signal type (0-3 [s1,s2,s3,s4])
     *  3 = sense type (walls, food, etc...)
     *  4 = secretion type (in the order seen in secretion droplist)
     *  5-8 = Channel 1-4 signal types (0-3)
     *  9 = max connections (20 by default)
     *  10 = Telomeres
     * */


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
        queuedChanges = new ArrayList<QueuedStaticChange>();
    }

    /**
     * Loads and (potentially) modifies static fields related to gene properties.
     * <p>
     * It is crucial that the {@code Gene} class is not loaded prior to this method's execution,
     * as its static block would initialize array properties to their default sizes,
     * and since they are final fields, the sizes of the arrays cannot be changed later.
     * </p>
     * @throws RuntimeException if reflection-based field access fails.
     */
    public static void loadStatic(){

        try {
            Field floatPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("A");
            Field floatPropertiesMinFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("z");
            Field intPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("w");

            intPropertiesMax = (int[]) intPropertiesMaxFieldPointer.get(null);
            floatPropertiesMin = (float[]) floatPropertiesMinFieldPointer.get(null);
            floatPropertiesMax = (float[]) floatPropertiesMaxFieldPointer.get(null);

            // Apply any patches to vanilla indices found in the queuedStaticChanges arraylist.
            for(QueuedStaticChange queuedChange : queuedChanges){
                switch (queuedChange.type()) {
                    case INTMAX: intPropertiesMax[queuedChange.index()] += (int) queuedChange.val();
                    case FMIN: floatPropertiesMin[queuedChange.index()] += (float) queuedChange.val();
                    case FMAX: floatPropertiesMax[queuedChange.index()] += (float) queuedChange.val();
                }
            }

            int j = 0;
            for (int i = ChimeraHooks.VANILLA_FPROPERTY_COUNT; i < fPropertiesCount; i++) {
                floatPropertiesMax[i] = modFloatProperties.get(j).getMaximumValue();
                floatPropertiesMin[i] = modFloatProperties.get(j).getMinimumValue();
                j++;
            }

            j = 0;
            for (int i = ChimeraHooks.VANILLA_IPROPERTY_COUNT; i < intPropertiesCount; i++) {
                intPropertiesMax[i] = modIntProperties.get(j).getMaximumValue();
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

    /**
     * Requests additional gene properties of a specified numeric type for a mod.
     * <p>
     * This method allows mods to create new gene properties by allocating additional slots
     * for either floating-point or integer properties. The request must be made during
     * mod initialization, any later invocation will be denied due to the nature of final static fields.
     * </p>
     * @param amount The number of additional gene properties to allocate.
     * @param type   The class type of the requested properties ({@code float.class} or {@code int.class}).
     * @param mod    The mod instance requesting the additional properties.
     * @return An array of newly allocated {@link GeneProperty} instances.
     * @throws RuntimeException if the request is made outside mod initialization or the mod reference is invalid.
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

    /**
     * Schedules a patch to the provided array at the given index to be applied as {@code Gene.class} is loaded into memory
     * at {@code loadStatic()}
     * @param type The static array as an entry on the StaticArray enum
     */
    protected static <T extends Number> void patchArray(int index, T value, StaticArray type){
        queuedChanges.add(new GeneAccess.QueuedStaticChange(index, value, type));
    }

}
