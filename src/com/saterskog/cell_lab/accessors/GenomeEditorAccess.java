package com.saterskog.cell_lab.accessors;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class GenomeEditorAccess extends Accessor{
    private final ArrayList<Object> controllers; // This is an arraylist of controllers such as seekbars and droplists, of type <com.saterskog.cell_lab.i>
    private static Class<?> ExtraSeekBarClass,GenomeEditorViewClass,CellTypesClass;

    public GenomeEditorAccess(Object genomeEditorView, ArrayList<Object> controllersList){
        super(genomeEditorView); //this object is a reference to <com.saterskog.cell_lab.i>
        controllers = controllersList;
    }

    public static void init(){
        try {
            ExtraSeekBarClass = Class.forName("com.saterskog.cell_lab.i$a");
            GenomeEditorViewClass = Class.forName("com.saterskog.cell_lab.i");
            CellTypesClass = Class.forName("com.saterskog.cell_lab.h");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void createSlider(String name, String description, int propertyIndex){
        try {
            Constructor<?> cons = ExtraSeekBarClass.getDeclaredConstructor(GenomeEditorViewClass,String.class,String.class,int.class,CellTypesClass);
            cons.setAccessible(true);
            Object extraSeekBar = cons.newInstance(this.getObjectReference(), name, description,propertyIndex,null);
            controllers.add(extraSeekBar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
