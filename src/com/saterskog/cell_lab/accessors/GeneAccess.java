package com.saterskog.cell_lab.accessors;

import com.saterskog.cell_lab.ChimeraHooks;
import com.saterskog.cell_lab.apiutils.RequestDeniedException;

import java.lang.reflect.Field;

public class GeneAccess {
    private float[] rgbColor,floatProperties;
    private Object geneObject;
    protected static int fPropertiesCount=8; //This controls the size of the float properties array. 8 by default, mods can add more.

    public GeneAccess(Object gene){
        this.geneObject = gene;

        try{
            Field rgbColorField = Class.forName("com.saterskog.cell_lab.Gene").getField("a");
            Field floatPropertiesField = Class.forName("com.saterskog.cell_lab.Gene").getField("v");

            this.rgbColor = (float[]) rgbColorField.get(geneObject);
            this.floatProperties = (float[]) floatPropertiesField.get(geneObject);
        }catch(Exception e){
            throw new RuntimeException();
        }
    }

    /** This method should only be called on mod initialization, either inside a static block or the mod constructor.
     * Failure to do so will result in a RequestDeniedException.
     */
    public static int[] requestAdditionalGeneFloatProperties(Object mod, int amount) throws RequestDeniedException {
        if(!ChimeraHooks.isCallerInitializer(1)){
            throw new RequestDeniedException("Request for additional float properties denied! Request must be made during mod initialization!"
                    + " culprit: " + mod.getClass().getSimpleName());
        }

        int[] indexArray = new int[amount];
        for(int i=0;i<amount;i++){indexArray[i]=fPropertiesCount+i;}
        fPropertiesCount = fPropertiesCount+amount;

        return indexArray; //returns an array of indices to the custom new properties.
    }
}
