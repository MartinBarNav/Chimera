package com.saterskog.cell_lab;

public class GeneProperty<T extends Number> {
    private int index;
    private T minimumValue, maximumValue;
    protected Object type;
    private boolean savable;

    protected GeneProperty(int index){
        this.index = index;
    }

    protected void setMinimumValue(T minimumValue) {
        this.minimumValue = minimumValue;
    }

    protected void setMaximumValue(T maximumValue){
        this.maximumValue = maximumValue;
    }

    public T getMinimumValue(){
        return this.minimumValue;
    }

    public T getMaximumValue(){
        return this.maximumValue;
    }

    public int getIndex(){
        return this.index;
    }

    public boolean isSavable(){
        return this.savable;
    }
}
