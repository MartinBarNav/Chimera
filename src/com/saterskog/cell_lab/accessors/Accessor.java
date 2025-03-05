package com.saterskog.cell_lab.accessors;

public abstract class Accessor {
    private Object objectReference; //This is a reference to the real object accessors abstract away.

    public Accessor(Object obj){
        this.objectReference = obj;
    }

    public Object getObjectReference() {
        return objectReference;
    }
}
