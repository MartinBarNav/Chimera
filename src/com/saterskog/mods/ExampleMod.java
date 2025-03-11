package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraHooks;
import com.saterskog.cell_lab.ChimeraMod;
import com.saterskog.cell_lab.accessors.CellAccess;
import com.saterskog.cell_lab.accessors.GeneAccess;
import com.saterskog.cell_lab.accessors.GeneProperty;
import com.saterskog.cell_lab.accessors.GenomeEditorAccess;

import java.io.IOException;
import java.io.ObjectInputStream;

@ChimeraMod
public class ExampleMod {
    private static GeneProperty<Float>[] myProperties;
    private static GeneProperty<Integer>[] myIntProperties;

    public ExampleMod(){
        myProperties = GeneAccess.requestAdditionalGeneProperties(1, float.class, this);
        myIntProperties = GeneAccess.requestAdditionalGeneProperties(1, int.class, this);
        GeneAccess.setMaximumValueOfProperty(myProperties[0], 77.0f);

        CellAccess.requestAdditionalModes(40, this);

        ChimeraHooks.enableSandbox();
    }

    public static void onCreateGenomeEditorViewHook(GenomeEditorAccess access){
        GenomeEditorAccess.Slider mySlider = GenomeEditorAccess.sliderBuilder()
                .name("Test")
                .description("Test description")
                .controlProperty(myProperties[0])
                .build();

        GenomeEditorAccess.Droplist myDroplist = GenomeEditorAccess.droplistBuilder()
                .name("Test")
                .description("Test description")
                .controlProperty(myIntProperties[0])
                .showModes(true)
                .build();

        access.showSlider(mySlider);
        access.showDroplist(myDroplist);
    }

    public static void onSaveGeneToStream(GeneAccess geneAccess) {
        geneAccess.savePropertiesToStream(myProperties, float.class);
        geneAccess.savePropertiesToStream(myIntProperties, int.class);
    }

    public void onLoadGeneFromStream(GeneAccess geneAccess){
        geneAccess.loadPropertiesFromStream(myProperties, float.class);
        geneAccess.loadPropertiesFromStream(myIntProperties, int.class);
    }

    public void onSaveGeneToParcel(GeneAccess geneAccess){
        geneAccess.savePropertiesToParcel(myProperties, float.class);
        geneAccess.savePropertiesToParcel(myIntProperties, int.class);
    }

    public void onLoadGeneFromParcel(GeneAccess geneAccess){
        geneAccess.loadPropertiesFromParcel(myProperties, float.class);
        geneAccess.loadPropertiesFromParcel(myIntProperties, int.class);
    }
}
