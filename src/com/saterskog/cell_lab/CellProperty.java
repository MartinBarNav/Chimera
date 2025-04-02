package com.saterskog.cell_lab;

public class CellProperty{
    private int index;
    enum Type{
        CELL_MODE,
        CELL_SIGNAL
    }
    private Type propertyType;

    protected CellProperty(int index, Type type){
        this.index = index;
        this.propertyType = type;
    }

    public int getIndex(){
        return this.index;
    }

    public Type getType(){
        return this.propertyType;
    }

}
