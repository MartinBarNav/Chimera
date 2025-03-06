package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraMod;
import com.saterskog.cell_lab.accessors.GeneAccess;
import com.saterskog.cell_lab.accessors.GeneProperty;
import com.saterskog.cell_lab.accessors.GenomeEditorAccess;

import java.io.IOException;
import java.io.ObjectInputStream;

@ChimeraMod
public class ExampleMod {
    private static GeneProperty<Float>[] myProperties;

    public ExampleMod(){
        myProperties = GeneAccess.requestAdditionalGeneProperties(1, float.class);
        GeneAccess.setMaximumValueOfProperty(myProperties[0], 77.0f);
    }

    public static void onCreateGenomeEditorViewHook(GenomeEditorAccess access){
        access.createSlider("Test","Test description",myProperties[0].getIndex());
    }

    public static void onSaveGeneToStream(GeneAccess geneAccess) {
        //geneAccess.savePropertiesToStream(myProperties, float.class);
    }

    public void onLoadGeneFromStream(GeneAccess geneAccess){
        //geneAccess.loadPropertiesFromStream(myProperties, float.class);
    }

    public void onSaveGeneToParcel(GeneAccess geneAccess){
        //geneAccess.savePropertiesToParcel(myProperties, float.class);
    }

    public void onLoadGeneFromParcel(GeneAccess geneAccess){
        //geneAccess.loadPropertiesFromParcel(myProperties, float.class);
    }
}
