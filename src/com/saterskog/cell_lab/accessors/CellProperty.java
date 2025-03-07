package com.saterskog.cell_lab.accessors;

public class CellProperty{
    private int index;
    private Object ownerMod;
    private GeneAccess geneReference;

    protected CellProperty(int index){
        this.index = index;
    }

    protected void setMod(Object mod){
        this.ownerMod = mod;
    }

    protected Object getOwnerMod(){
        return this.ownerMod;
    }
}
