package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraMod;
import com.saterskog.cell_lab.accessors.GeneAccess;
import com.saterskog.cell_lab.accessors.GeneProperty;
import com.saterskog.cell_lab.accessors.GenomeEditorAccess;

@ChimeraMod
public class ExampleMod {
    private static GeneProperty<Float>[] myProperties;

    public ExampleMod(){
        myProperties = GeneAccess.requestAdditionalGeneFloatProperties(this, 1);
        GeneAccess.setMaximumValueOfProperty(myProperties[0], 77.0f);
    }

    public static void onCreateGenomeEditorViewHook(GenomeEditorAccess access){
        access.createSlider("Test","Test description",myProperties[0].getIndex());
    }
}
