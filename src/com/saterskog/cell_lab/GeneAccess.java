package com.saterskog.cell_lab;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class GeneAccess extends Accessor<Object>{
    protected static int fPropertiesCount=ChimeraHooks.VANILLA_FPROPERTY_COUNT,intPropertiesCount=ChimeraHooks.VANILLA_IPROPERTY_COUNT;
    private static final ArrayList<GeneProperty<Float>> modFloatProperties = new ArrayList<>();;
    private static final ArrayList<GeneProperty<Integer>> modIntProperties = new ArrayList<>();;
    private int formatVersion;

    // Fields in the Gene class whose values can be initialized in a method other than where the changes are added to the queue.
    // If the field is a static final, the changes are scheduled for class loading.
    // If the field is non-static and non-final, the changes are applied at a different method. Eg, onGeneInit() for GeneProperties.
    public enum Queueable{
        INTMAX,
        FMIN,
        FMAX,
        F_GENE_PROPERTY,
        INT_GENE_PROPERTY
    }
    /**
     * Represents a pending modification to an array, specifying the index at which a new value should be inserted.
     * These modifications are collected in a list (or queue) and applied all at once during <s>class loading</s>* to patch field contents.
     * <p>* for some Queueable types, changes are applied more dynamically, not necessarily during class loading.</p>
     * @param val The type of value to be inserted, subclass of {@link Number}.
     * @param type an entry in the StaticArray enum mapped to the target static array
     */
    protected record QueuedChange(int index, Number val, Queueable type) {}
    private static ArrayList<QueuedChange> queuedChanges = new ArrayList<>();;

    //Cell lab field references
    protected float[] rgbColor,floatProperties; //a[4],v[7]
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

    protected int[] intProperties;//u[11]
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

    //Methods
    private static Method loadGeneMethod;
    private static Method saveGeneMethod;

    //Class names
    protected static final String geneClassName = "com.saterskog.cell_lab.Gene";


    protected GeneAccess(Object gene, AndroidAccess parcel) throws RuntimeException {
        this(gene);
        setParcel(parcel);
    }

    protected GeneAccess(Object gene, ObjectInputStream stream, int version) {
        this(gene);
        this.formatVersion = version;
        setInStream(stream);
    }

    protected GeneAccess(Object gene, ObjectOutputStream stream) {
        this(gene);
        setOutStream(stream);
    }

    protected GeneAccess(Object gene){
        super(gene);

        try{
            Class<?> geneClass = Class.forName(geneClassName);
            Field rgbColorFieldPointer = geneClass.getField("a");
            Field floatPropertiesFieldPointer = geneClass.getField("v");
            Field intPropertiesFieldPointer = geneClass.getField("u");

            this.rgbColor = (float[]) rgbColorFieldPointer.get(this.getObjectReference());
            this.floatProperties = (float[]) floatPropertiesFieldPointer.get(this.getObjectReference());
            this.intProperties = (int[]) intPropertiesFieldPointer.get(this.getObjectReference());

            loadGeneMethod = geneClass.getMethod("a", ObjectInputStream.class, int.class);
            saveGeneMethod = geneClass.getMethod("a",ObjectOutputStream.class);
        }catch(Exception e){
            throw new RuntimeException();
        }
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
    protected static void loadStatic(){
        try {
            Field floatPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("A");
            Field floatPropertiesMinFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("z");
            Field intPropertiesMaxFieldPointer = Class.forName("com.saterskog.cell_lab.Gene").getField("w");

            intPropertiesMax = (int[]) intPropertiesMaxFieldPointer.get(null);
            floatPropertiesMin = (float[]) floatPropertiesMinFieldPointer.get(null);
            floatPropertiesMax = (float[]) floatPropertiesMaxFieldPointer.get(null);

            // Apply any patches to vanilla indices found in the queuedStaticChanges arraylist.
            for(QueuedChange queuedChange : queuedChanges){
                switch (queuedChange.type()) {
                    case INTMAX:
                        intPropertiesMax[queuedChange.index()] += (int) queuedChange.val();
                        break;
                    case FMIN:
                        floatPropertiesMin[queuedChange.index()] += (float) queuedChange.val();
                        break;
                    case FMAX:
                        floatPropertiesMax[queuedChange.index()] += (float) queuedChange.val();
                        break;
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

    //For safety, this returns a copy of the queuedChanges, not a reference.
    public static ArrayList<QueuedChange> getQueuedChanges(){ return new ArrayList<>(queuedChanges);}

    public static <T extends Number> void setMinimumValueOfProperty(GeneProperty<T> property, T value){
        if(Chimera.isCallerInitializer(1)) {
            property.setMinimumValue(value);
            if(value instanceof Float){
                modFloatProperties.get(property.getIndex()-ChimeraHooks.VANILLA_FPROPERTY_COUNT).setMinimumValue((float) value);
            }
        }

    }

    /**
     * Use only if you explicitly want to set vanilla properties!
     * <p>For a safer approach, use the {@code setValueOf(GeneProperty<T> property, T value)} method to set modded properties!
     */
    public void setValueOf(int index, Number value){
        if(value instanceof Integer){
            if(index >= intPropertiesCount) return;
            intProperties[index] = (int) value;
        }
        else if(value instanceof  Float){
            if(index >= fPropertiesCount) return;
            floatProperties[index] = (float) value;
        }
    }

    /**
     * Use only if you explicitly want to read vanilla gene properties!
     * <p>For a safer approach, use the {@code getValueOf(GeneProperty<T> property)} method to read modded properties!
     */
    public Number getValueOf(int index, Class<?> type){
        if(type == int.class){
            if(index >= intPropertiesCount) return null;
            return intProperties[index];
        }
        else if(type == float.class){
            if(index >= fPropertiesCount) return null;
            return floatProperties[index];
        }
        return null;
    }

    public <T extends Number> void setValueOf(GeneProperty<T> property, T value){
        setValueOf(property.getIndex(),value);
    }

    public Number getValueOf(GeneProperty<? extends Number> property){
        return getValueOf(property.getIndex(),property.type.getClass());
    }

    //Uses queued changes to schedule property[i]=value. For GeneProperties, this is applied on the onGeneInit() hook in ChimeraHooks.java
    public static <T extends Number> void setDefaultValueOfProperty(GeneProperty<T> property, T value){
        Queueable type = value instanceof Float ? Queueable.F_GENE_PROPERTY : Queueable.INT_GENE_PROPERTY;
        queueChange(property.getIndex(),value, type);
    }

    public static <T extends Number> void setMaximumValueOfProperty(GeneProperty<T> property, T value){
        if(Chimera.isCallerInitializer(1)) {
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
    public static <T extends Number> GeneProperty<T>[] requestAdditionalGeneProperties(int amount, Class<T> type, Object caller) {

        if(!Chimera.validateRequest(1,caller)){
            throw new RuntimeException("Request for additional properties DENIED!" + " caller must be constructor or static block" +
                    " belonging to the main mod class!\nculprit: "+ caller.getClass());
        }

        GeneProperty<T>[] properties = new GeneProperty[amount];

        for (int i = 0; i < amount; i++) {
            if (type == float.class) {
                properties[i] = new GeneProperty<>(fPropertiesCount + i);
                properties[i].type = float.class;
                properties[i].setMaximumValue((T) Float.valueOf(1.f));
                properties[i].setMinimumValue((T) Float.valueOf(0.f));
                modFloatProperties.add((GeneProperty<Float>) properties[i]);
            } else if (type == int.class) {
                properties[i] = new GeneProperty<>(intPropertiesCount + i);
                properties[i].type = int.class;
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

    public void invokeMethod(String name, Object... args) throws InvocationTargetException, IllegalAccessException {
        switch(name){
            case "loadGene":
                loadGeneMethod.invoke(this.getObjectReference(),args);
                break;
            case "saveGene":
                saveGeneMethod.invoke(this.getObjectReference(),args);
                break;
        }
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
                Chimera.invokeMethod(this.getParcel().getObjectReference(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(property.type == int.class){
                Chimera.invokeMethod(this.getParcel().getObjectReference(), "writeInt", new Class[]{int.class},
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
                this.floatProperties[property.getIndex()] = (float) Chimera.invokeMethod(this.getParcel().getObjectReference(), "readFloat");
            }
            else if(property.type == int.class){
                this.intProperties[property.getIndex()] = (int) Chimera.invokeMethod(this.getParcel().getObjectReference(), "readInt");
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
                Chimera.invokeMethod(this.getOutStream(), "writeFloat", new Class[]{float.class},
                        this.floatProperties[property.getIndex()]);
            }
            else if(property.type == int.class){
                Chimera.invokeMethod(this.getOutStream(), "writeInt", new Class[]{int.class},
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
                Chimera.logException(e);
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * If a final static array is the target, this method schedules a patch to the provided array at the given index to be applied as
     * {@code Gene.class} is loaded into memory at {@code loadStatic()}
     * <p>However, other non-final or non-static targets (such as GeneProperties) will have queued changes applied in their respective initializing
     * methods. </p>
     * @param type The static array as an entry on the StaticArray enum
     */
    protected static <T extends Number> void queueChange(int index, T value, Queueable type){
        queuedChanges.add(new QueuedChange(index, value, type));
    }

}
