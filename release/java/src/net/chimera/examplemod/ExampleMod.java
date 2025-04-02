package net.chimera.examplemod;

import com.saterskog.cell_lab.*;
import com.saterskog.cell_lab.Chimera;

@ModMain
public class ExampleMod {
    public ExampleMod(Mod mod){
        mod.registerHookListener(this.getClass());

        Chimera.enableSandbox();
        CellAccess.requestAdditionalModes(40,this);
    }

    @Hook
    public static void onCreateLabFragment(){
        Chimera.logMessage("tedst");
    }
}
