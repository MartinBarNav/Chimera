package com.saterskog.cell_lab;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class GenomeEditorAccess {
    private final Object genomeEditorObject; //this is a reference to <com.saterskog.cell_lab.i>
    private final ArrayList<Object> controllers; // This is an arraylist of controllers such as seekbars and droplists, of type <com.saterskog.cell_lab.i>
    private final Class<?> ExtraSeekBarClass,GenomeEditorViewClass,CellTypesClass;

    public GenomeEditorAccess(Object genomeEditorView, ArrayList<Object> controllersList){
        genomeEditorObject = genomeEditorView;
        controllers = controllersList;

        try {
            this.ExtraSeekBarClass = Class.forName("com.saterskog.cell_lab.i$a");
            this.GenomeEditorViewClass = Class.forName("com.saterskog.cell_lab.i");
            this.CellTypesClass = Class.forName("com.saterskog.cell_lab.h");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find subclasses");
            throw new RuntimeException(e);
        }
    }

    public void createSlider(String name, String description){
        try {
            Constructor<?> cons = ExtraSeekBarClass.getConstructor(GenomeEditorViewClass,String.class,String.class,int.class,CellTypesClass);
            Object extraSeekBar = cons.newInstance(genomeEditorObject, name, description,1,null);
            controllers.add(extraSeekBar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
