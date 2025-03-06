package com.saterskog.cell_lab.accessors;

import java.io.ObjectInputStream;

//Wrapper
public class GeneProperty<T> {
    private int index;
    private T minimumValue, maximumValue;
    private Object ownerMod;

    protected GeneProperty(int index){
        this.index = index;
    }

    protected void setMod(Object mod){
        this.ownerMod = mod;
    }

    protected Object getMod(){
        return this.ownerMod;
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

    public void readFromStream(ObjectInputStream stream){

    }
}
