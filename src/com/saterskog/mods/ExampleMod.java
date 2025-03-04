package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraMod;
import com.saterskog.cell_lab.GenomeEditorAccess;

@ChimeraMod
public class ExampleMod {

    public static boolean unlockChallengeHook(int challengeId){
        return true;
    }

    public static void onCreateGenomeEditorViewHook(GenomeEditorAccess access){
        return;
    }
}
