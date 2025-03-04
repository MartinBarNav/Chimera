package com.saterskog.cell_lab;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class GenomeEditorAccess {
    private final Object genomeEditorObject; //this is a reference to <com.saterskog.cell_lab.i>
    private final ArrayList<Object> controllers; // This is an arraylist of controllers such as seekbars and droplists, of type <com.saterskog.cell_lab.i>
    private Class<?> ExtraSeekBarClass;

    public GenomeEditorAccess(Object genomeEditorView, ArrayList<Object> controllersList){
        genomeEditorObject = genomeEditorView;
        controllers = controllersList;

        try {
            this.ExtraSeekBarClass = Class.forName("com.saterskog.cell_lab.i$a");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find subclass a of i");
            this.ExtraSeekBarClass = null;
        }
    }

    public void createSlider(String name, String description){
        try {
            Constructor<?> cons = ExtraSeekBarClass.getConstructor(Object.class,String.class,String.class,int.class,Object.class);
            Object extraSeekBar = cons.newInstance(genomeEditorObject, name, description,1,null);
            controllers.add(extraSeekBar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
