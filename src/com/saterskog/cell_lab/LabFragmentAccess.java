package com.saterskog.cell_lab;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class LabFragmentAccess extends Accessor<Object>{
    private ArrayList<Object> controllers; // This is an arraylist of controllers such as seekbars and droplists
    protected static Class<?> ExtraSeekBarClass, LabFragmentClass,CellTypesClass,DroplistClass;
    protected static Field genesArray, genomeName;
    private static String[] modesPrefix;

    public enum View{
        BUTTON_LOAD_GENOME(2131230743),
        BUTTON_SAVE_GENOME(2131230747);
        private int id;
        View(int val){
            this.id=val;
        }

        public int getID(){
            return this.id;
        }
    }

    protected LabFragmentAccess(AndroidAccess.Fragment labFragment, ArrayList<Object> controllersList, String[] modesString){
        this(labFragment);
        controllers = controllersList;
        modesPrefix=modesString;
    }

    protected LabFragmentAccess(AndroidAccess.Fragment labFragment){
        super(labFragment.getObjectReference()); //this object should be a reference to <com.saterskog.cell_lab.i>
    }

    protected static void init(){
        try {
            ExtraSeekBarClass = Class.forName("com.saterskog.cell_lab.i$a");
            LabFragmentClass = Class.forName("com.saterskog.cell_lab.i");
            DroplistClass = Class.forName("com.saterskog.cell_lab.i$c");
            CellTypesClass = Class.forName("com.saterskog.cell_lab.h");

            //Fields
            //getField() just doesn't retrieve private fields and these are private.
            genesArray = LabFragmentClass.getDeclaredField("a");
            genesArray.setAccessible(true);
            genomeName = LabFragmentClass.getDeclaredField("d");
            genomeName.setAccessible(true);
        } catch (Exception e) {
            Chimera.logException(e);
            throw new RuntimeException(e);
        }
    }

    public static Slider.Builder sliderBuilder(){
        return new Slider.Builder();
    }

    public static Droplist.Builder droplistBuilder(){
        return new Droplist.Builder();
    }

    public void showSlider(Slider slider){
        try {
            Constructor<?> cons = ExtraSeekBarClass.getDeclaredConstructor(LabFragmentClass,String.class,String.class,int.class,CellTypesClass);
            cons.setAccessible(true);
            Object extraSeekBar = cons.newInstance(this.getObjectReference(), slider.name, slider.description,slider.propertyIndex,null);
            controllers.add(extraSeekBar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showDroplist(Droplist list){
        try {
            Constructor<?> cons = DroplistClass.getDeclaredConstructor(LabFragmentClass,String.class,
                    String.class,int.class,CellTypesClass,String[].class,int.class,boolean.class);
            cons.setAccessible(true);
            Object extraDroplist = cons.newInstance(this.getObjectReference(), list.name, list.description,list.propertyIndex,null,
                    list.contentLabels,list.unlockedAt,list.modes);
            controllers.add(extraDroplist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Droplist{
        private String name, description;
        private int propertyIndex;
        //private Object cellType; TODO: add later
        private String[] contentLabels;
        private boolean modes;
        private int unlockedAt;

        private Droplist(Builder builder){
            this.name = builder.name; this.description = builder.description;
            this.propertyIndex = builder.propertyIndex;
            this.contentLabels = builder.contentLabels;
            this.modes = builder.modes;
            this.unlockedAt = builder.unlockedAt;
        }

        public static class Builder{
            private String name="name", description="description";
            private int propertyIndex=0;
            private String[] contentLabels= new String[]{"contents"};
            private boolean modes=false;
            private int unlockedAt=-1;

            public Builder name(String s){
                this.name = s;
                return this;
            }

            public Builder description(String s){
                this.description = s;
                return this;
            }

            public Builder controlProperty(GeneProperty<Integer> property){
                this.propertyIndex = property.getIndex();
                return this;
            }

            public Builder setItems(String[] l){
                this.contentLabels = l;
                this.modes=false;
                return this;
            }

            public Builder showModes(){
                this.modes = true;
                this.contentLabels=modesPrefix;
                return this;
            }

            public Builder unlockedAt(int i){
                this.unlockedAt = i;
                return this;
            }

            public Droplist build(){
                return new Droplist(this);
            }
        }
    }

    public static class Slider{
        private String name, description;
        private int propertyIndex;
        //private Object cellType; TODO: add later

        private Slider(Builder builder){
            this.name = builder.name;
            this.description = builder.description;
            this.propertyIndex = builder.propertyIndex;
        }

        public static class Builder{
            private String name="name", description="description";
            private int propertyIndex=0;

            public Builder name(String s){
                this.name = s;
                return this;
            }

            public Builder description(String s){
                this.description = s;
                return this;
            }

            public Builder controlProperty(GeneProperty<Float> property){
                this.propertyIndex= property.getIndex();
                return this;
            }

            public Slider build(){
                return new Slider(this);
            }
        }
    }

}
