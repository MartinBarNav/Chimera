package com.saterskog.cell_lab;

import com.saterskog.cell_lab.accessors.*;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChimeraHooks {
    private static boolean initialized=false;
    public static final int VANILLA_FPROPERTY_COUNT=7,VANILLA_IPROPERTY_COUNT=11,VANILLA_MODES_COUNT=40,VANILLA_SIGNAL_COUNT=4,
    VANILLA_VERSION = 95, VANILLA_SECRETEABLE_CHEMICALS=7;
    protected static int modFormatVersion=VANILLA_VERSION;
    protected static boolean SandboxMode=false;

    private static final List<Mod> mods = new ArrayList<>();

    protected static void initMods(Object appActivity) {
        if(initialized) return;

        String modsPath = AndroidAccess.getExternalFilesDir(appActivity).getAbsolutePath()+"/mods";
        List<String> modDirectory = AndroidAccess.listFiles(new File(modsPath));

        if(modDirectory.isEmpty()){
            Chimera.logMessage("Found no mods to load.");
            initialized=true;
            return;
        }

        Chimera.logMessage("initializing accessors...");
        GenomeEditorAccess.init();

        for(String file : modDirectory){
            if(file.endsWith(".jar")){
                Map<String,String> manifestContents = AndroidAccess.readModManifest(new File(modsPath+"/"+file));
                if(Mod.validateManifest(manifestContents)){
                    Mod mod = new Mod(manifestContents);
                    //Load mod class and instantiate it, using DexClassLoader
                    Object dexClassLoader = AndroidAccess.dexClassLoaderInit(modsPath+"/"+file, AndroidAccess.getClassLoader(appActivity));
                    Class<?> modClass = AndroidAccess.loadClass(dexClassLoader,mod.getMainClassName());
                    try {
                        Constructor<?> cons = modClass.getDeclaredConstructor(Mod.class);
                        cons.setAccessible(true);
                        cons.newInstance(mod);
                        mods.add(mod);
                    } catch (Exception e) {
                        Chimera.logException(e);
                        Chimera.logError("Failed to load mod: "+mod.getID()+" crash averted.");
                    }
                }
                else{
                    if(manifestContents.isEmpty()){
                        Chimera.logMessage("Mod manifest not found!");
                    }else {
                        Chimera.logMessage("Invalid mod manifest!: " + manifestContents);
                    }
                }
            }
        }

        GeneAccess.loadStatic();
        initialized=true;
    }

    public static void enableSandbox(){
        SandboxMode=true;
    }

    protected static void loadCellFromStream(Object cell, ObjectInputStream stream, int version){
        CellAccess access = new CellAccess(cell,stream,version);
        invokeModImplementationWithAccess("onLoadCellFromStream", access);
    }

    protected static void saveCellToStream(Object cell, ObjectOutputStream stream){
        CellAccess access = new CellAccess(cell,stream);
        invokeModImplementationWithAccess("onSaveCellToStream", access);
    }

    protected static void loadGeneFromStream(Object gene, ObjectInputStream stream, int version){
        GeneAccess access = new GeneAccess(gene, stream, version);
        invokeModImplementationWithAccess("onLoadGeneFromStream", access);
    }

    protected static void saveGeneToStream(Object gene, ObjectOutputStream stream){
        GeneAccess access = new GeneAccess(gene, stream);
        invokeModImplementationWithAccess("onSaveGeneToStream",access);
    }

    protected static void loadGeneFromParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);
        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        invokeModImplementationWithAccess("onLoadGeneFromParcel", access);
    }

    protected static void saveGeneToParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);
        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        invokeModImplementationWithAccess("onSaveGeneToParcel",access);
    }

    protected static void onCreateGEditorHook(Object geditorView, ArrayList<Object> controllers, String[] modesString){
        GenomeEditorAccess access = new GenomeEditorAccess(geditorView, controllers, modesString);
        invokeModImplementationWithAccess("onCreateGenomeEditorView",access);
    }

    // Reflection utilities
    private static <T extends Accessor> void invokeModImplementationWithAccess(String methodName, T access) {
        invokeAllModImplementations(methodName, access);
    }

    private static void invokeAllModImplementations(String methodName, Object... args) {
        if (mods.isEmpty()) return;

        for (Mod mod : mods) {
            Method hook = mod.getHook(methodName);
            if(hook != null){
                try {
                    hook.setAccessible(true);
                    if(Modifier.isStatic(hook.getModifiers())) {
                        hook.invoke(null, args);
                    }else{
                        Chimera.logError("Cannot invoke "+methodName+" implementation because it is not static. Hooks must be made static!");
                    }
                } catch (Exception e) {
                    Chimera.logException(e);
                }
            }
        }
    }
}
