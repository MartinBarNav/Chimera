package com.saterskog.cell_lab;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChimeraHooks {
    private static boolean initialized=false;
    protected static final int VANILLA_FPROPERTY_COUNT=7,VANILLA_IPROPERTY_COUNT=11,VANILLA_MODES_COUNT=40,VANILLA_SIGNAL_COUNT=4,
    VANILLA_VERSION = 95, VANILLA_SECRETEABLE_CHEMICALS=7;
    protected static int cachedFormatVersion;
    protected static int chimeraMagicNumber=0xAABB;
    protected static boolean SandboxMode=false;

    private static final List<Mod> mods = new ArrayList<>();
    protected static boolean wroteHeader=false, readHeader=false; //This prevents Mod.Header from being serialized or read more than once per file


    protected static void initMods(Object appActivity) {
        if(initialized) return;

        AndroidAccess.AppActivity activity = new AndroidAccess.AppActivity(appActivity);

        AndroidAccess.makeToast(activity,"Powered by Chimera Modloader", false);

        String modsPath = AndroidAccess.getExternalFilesDir(activity).getAbsolutePath()+"/mods";
        List<String> modDirectory = AndroidAccess.listFiles(new File(modsPath));

        if(modDirectory.isEmpty()){
            AndroidAccess.makeToast(activity,"No mods installed", false);
            Chimera.logMessage("Found no mods to load.");
            initialized=true;
            return;
        }

        Chimera.logMessage("initializing accessors...");
        LabFragmentAccess.init();

        for(String file : modDirectory){
            if(file.endsWith(".jar")){
                Map<String,String> manifestContents = AndroidAccess.readModManifest(new File(modsPath+"/"+file));
                if(Mod.validateManifest(manifestContents)){
                    Mod mod = new Mod(manifestContents);
                    //Load mod class and instantiate it, using DexClassLoader
                    try {
                        Object dexClassLoader = AndroidAccess.dexClassLoaderInit(modsPath+"/"+file, AndroidAccess.getClassLoader(activity));
                        Class<?> modClass = AndroidAccess.loadClass(dexClassLoader,mod.getMainClassName());
                        Constructor<?> cons = modClass.getDeclaredConstructor(Mod.class);
                        cons.setAccessible(true);
                        cons.newInstance(mod);
                        mods.add(mod);
                    } catch (Exception e) {
                        Chimera.logException(e);
                        AndroidAccess.makeToast(activity,"Failed to load mod ["+mod.getID()+"] caused by: "+e.getCause(), true);
                    }
                }
                else{
                    if(manifestContents.isEmpty()){
                        Chimera.logMessage("Mod manifest not found!");
                    }else {
                        Chimera.logMessage("Invalid mod manifest!: " + manifestContents);
                    }
                    AndroidAccess.makeToast(activity,"Could not load unknown mod, badly formatted", false);
                }
            }
        }

        GeneAccess.loadStatic();
        CellAccess.loadStatic();
        initialized=true;
    }

    protected static void unloadMod(Mod mod){
        mods.remove(mod);
    }

    protected static void onCopyGene(Object gene1, Object gene2){
        GeneAccess thisGene = new GeneAccess(gene1);
        GeneAccess otherGene = new GeneAccess(gene2);
        invokeAllModImplementations("onCopyGenes",thisGene,otherGene);

        if(GeneAccess.fPropertiesCount != VANILLA_FPROPERTY_COUNT){
            short copyIndex = VANILLA_FPROPERTY_COUNT;
            int copyAmount = GeneAccess.fPropertiesCount - VANILLA_FPROPERTY_COUNT;
            System.arraycopy(otherGene.floatProperties,copyIndex,thisGene.floatProperties,copyIndex,copyAmount);
        }
        if(GeneAccess.intPropertiesCount != VANILLA_IPROPERTY_COUNT){
            short copyIndex = VANILLA_IPROPERTY_COUNT;
            int copyAmount = GeneAccess.intPropertiesCount - VANILLA_IPROPERTY_COUNT;
            System.arraycopy(otherGene.intProperties,copyIndex,thisGene.intProperties,copyIndex,copyAmount);
        }
    }
    protected static void onLoadGenomeButtonClicked(Object labFragment, int itemIndex){
        readHeader=false;
    }

    protected static void onSaveGenomeButtonClicked(Object labFragment, String genomeName){
        wroteHeader=false;
    }

    protected static void onReadGenomeVersion(Object labFragment, ObjectInputStream stream, int version){
        if(readHeader) return;

        cachedFormatVersion = version;
        AndroidAccess.Fragment fragment = new AndroidAccess.Fragment(labFragment);

        if(version != chimeraMagicNumber){
            AndroidAccess.makeToast(fragment.getActivity(), "Loading genome in vanilla compatibility mode...", false);
            return;
        }

        try {
            Mod.Header modHeader = (Mod.Header) stream.readObject();
            AndroidAccess.makeToast(fragment.getActivity(), "necessary mods: "+modHeader.necessaryMods.toString()
                    +" modes:"+modHeader.modesCount+" signals:"+modHeader.signalCount, true);
        } catch (Exception e) {
            Chimera.logException(e);
            throw new RuntimeException(e);
        }
        readHeader=true;
    }

    protected static void onWriteGenomeVersion(Object labFragment, ObjectOutputStream stream){
        if(wroteHeader) return;

        AndroidAccess.Fragment fragment = new AndroidAccess.Fragment(labFragment);
        ArrayList<String> modIds = new ArrayList<>();
        int[] gameConstants = new int[]{CellAccess.modesCount, CellAccess.signalCount};
        for(Mod mod : mods){
            modIds.add(mod.getID());
        }
        try {
            stream.writeObject(new Mod.Header(modIds, gameConstants));
            AndroidAccess.makeToast(fragment.getActivity(),modIds.toString(),true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        wroteHeader=true;
    }
    protected static void onLoadGenome(Object labFragment, ObjectInputStream stream){
        AndroidAccess.Fragment fragment = new AndroidAccess.Fragment(labFragment);
        LabFragmentAccess access = new LabFragmentAccess(fragment);

        //TODO, make this load however many modes modHeader says there are in the savefile, NOT how many there currently are in-game
        if(CellAccess.modesCount != VANILLA_MODES_COUNT){
            for(int i=VANILLA_MODES_COUNT;i<CellAccess.modesCount;i++){
                try {
                    Object[] geneArray = (Object[]) LabFragmentAccess.genesArray.get(access.getObjectReference());
                    GeneAccess gene = new GeneAccess(geneArray[i]);
                    gene.invokeMethod("loadGene",stream,chimeraMagicNumber);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    protected static void onSaveGenome(Object labFragment, ObjectOutputStream stream){
        AndroidAccess.Fragment fragment = new AndroidAccess.Fragment(labFragment);
        LabFragmentAccess access = new LabFragmentAccess(fragment);

        if(CellAccess.modesCount != VANILLA_MODES_COUNT){
            for(int i=VANILLA_MODES_COUNT;i<CellAccess.modesCount;i++){
                try {
                    Object[] geneArray = (Object[]) LabFragmentAccess.genesArray.get(access.getObjectReference());
                    GeneAccess gene = new GeneAccess(geneArray[i]);
                    gene.invokeMethod("saveGene",stream);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected static void onGeneInit(Object labFragment, Object gene){
        GeneAccess geneAccess = new GeneAccess(gene);
        invokeAllModImplementations("onGeneInit", geneAccess);

        //Applies changes that were queued for GeneProperties. Effectively setting a default value for GeneProperties.
        for(GeneAccess.QueuedChange queuedChange : GeneAccess.getQueuedChanges()){
            switch(queuedChange.type()){
                case F_GENE_PROPERTY:
                case INT_GENE_PROPERTY: geneAccess.setValueOf(queuedChange.index(), queuedChange.val());
            }
        }
    }

    protected static void loadCellFromStream(Object cell, ObjectInputStream stream, int version){
        CellAccess access = new CellAccess(cell,stream,version);
        invokeAllModImplementations("onLoadCellFromStream", access);
    }

    protected static void saveCellToStream(Object cell, ObjectOutputStream stream){
        CellAccess access = new CellAccess(cell,stream);
        invokeAllModImplementations("onSaveCellToStream", access);
    }

    protected static void loadGeneFromStream(Object gene, ObjectInputStream stream, int version){
        GeneAccess access = new GeneAccess(gene, stream, version);
        invokeAllModImplementations("onLoadGeneFromStream", access);
    }

    protected static void saveGeneToStream(Object gene, ObjectOutputStream stream){
        GeneAccess access = new GeneAccess(gene, stream);
        invokeAllModImplementations("onSaveGeneToStream",access);
    }

    protected static void loadGeneFromParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);
        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        invokeAllModImplementations("onLoadGeneFromParcel", access);
    }

    protected static void saveGeneToParcel(Object gene, Object parcel){
        AndroidAccess parcelAccess = new AndroidAccess(parcel, AndroidAccess.Type.PARCEL);
        GeneAccess access =  new GeneAccess(gene,parcelAccess);
        invokeAllModImplementations("onSaveGeneToParcel",access);
    }

    protected static void onCreateLabFragment(Object labFragment, ArrayList<Object> controllers, String[] modesString){
        LabFragmentAccess access = new LabFragmentAccess(new AndroidAccess.Fragment(labFragment), controllers, modesString);
        invokeAllModImplementations("onCreateGenomeEditorView",access);
    }

    // Reflection utilities
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
                    unloadMod(mod);
                    Chimera.logException(e);
                }
            }
        }
    }
}
