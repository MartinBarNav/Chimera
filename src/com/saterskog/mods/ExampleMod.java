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

    public static void onCreateGenomeEditorView(GenomeEditorAccess access){
        GenomeEditorAccess.Slider mySlider = GenomeEditorAccess.sliderBuilder()
                .name("My Slider")
                .description("Test description")
                .controlProperty(myProperties[0])
                .build();

        GenomeEditorAccess.Droplist myDroplist = GenomeEditorAccess.droplistBuilder()
                .name("My Droplist")
                .description("Test description")
                .controlProperty(myIntProperties[0])
                .showModes()
                .build();

        access.showSlider(mySlider);
        access.showDroplist(myDroplist);
    }

    public static void onSaveGeneToStream(GeneAccess geneAccess) {
        geneAccess.savePropertiesToStream(myProperties);
        geneAccess.savePropertiesToStream(myIntProperties);
    }

    public void onLoadGeneFromStream(GeneAccess geneAccess){
        if(geneAccess.getFormatVersion() == ChimeraHooks.getCurrentFormatVersion()) {
            geneAccess.loadPropertiesFromStream(myProperties);
            geneAccess.loadPropertiesFromStream(myIntProperties);
        }
    }

    public void onSaveGeneToParcel(GeneAccess geneAccess){
        geneAccess.savePropertiesToParcel(myProperties);
        geneAccess.savePropertiesToParcel(myIntProperties);
    }

    public void onLoadGeneFromParcel(GeneAccess geneAccess){
        geneAccess.loadPropertiesFromParcel(myProperties);
        geneAccess.loadPropertiesFromParcel(myIntProperties);
    }
}
