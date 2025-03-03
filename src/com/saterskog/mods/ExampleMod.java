package com.saterskog.mods;

import com.saterskog.cell_lab.ChimeraMod;

@ChimeraMod
public class ExampleMod {

    public boolean unlockChallengeHook(int challengeId){
        return challengeId > 6;
    }
}
