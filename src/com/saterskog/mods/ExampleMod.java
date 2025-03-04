package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraMod;
import com.saterskog.cell_lab.GenomeEditorAccess;

@ChimeraMod
public class ExampleMod {
    public static void onCreateGenomeEditorViewHook(GenomeEditorAccess access){
        access.createSlider("Test","Test description");
    }
}
