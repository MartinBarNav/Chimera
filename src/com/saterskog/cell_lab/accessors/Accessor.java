package com.saterskog.cell_lab.accessors;


import com.saterskog.cell_lab.ChimeraHooks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Accessor {
    private AndroidAccess parcel;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    private Object objectReference; //This is a reference to the real object accessors abstract away.


    protected Accessor(Object obj){
        this.objectReference = obj;
    }

    protected Object getObjectReference() {
        return objectReference;
    }

    protected void setParcel(AndroidAccess access) throws RuntimeException {
        if(access.getType() == AndroidAccess.Type.PARCEL) {
            this.parcel = access;
        } else{
            throw new RuntimeException("Accessor.setParcel() was not given a valid parcel object!");
        }
    }

    protected AndroidAccess getParcel(){
        return this.parcel;
    }

    protected void setInStream(ObjectInputStream stream){
        this.inStream = stream;
    }

    protected void setOutStream(ObjectOutputStream stream){
        this.outStream = stream;
    }

    protected ObjectOutputStream getOutStream(){
        return this.outStream;
    }

    protected ObjectInputStream getInStream(){
        return this.inStream;
    }

}
