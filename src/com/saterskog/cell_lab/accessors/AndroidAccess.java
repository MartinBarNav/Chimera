package com.saterskog.cell_lab.accessors;

public class AndroidAccess extends Accessor{
    public static enum Type{
        PARCEL
    }

    private Type accessType;

    public AndroidAccess(Object obj, Type type) {
        super(obj);
        this.accessType = type;
    }

    public Type getType(){
        return this.accessType;
    }
}
