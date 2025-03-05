package com.saterskog.cell_lab.accessors;

//Wrapper
public class GeneProperty<T> {
    private int index;
    private T minimumValue, maximumValue;

    protected GeneProperty(int index){
        this.index = index;
    }

    public void setMinimumValue(T minimumValue) {
        this.minimumValue = minimumValue;
    }

    public void setMaximumValue(T maximumValue){
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
}
