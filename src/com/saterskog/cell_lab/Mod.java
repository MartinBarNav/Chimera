package com.saterskog.cell_lab;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mod{
    private final Map<String,String> info;
    private final List<Class<?>> hookListeners = new ArrayList<>();
    private final Map<String,Method> hooks = new HashMap<>(); //hook name and it's associated method

    public Mod(Map<String,String> manifest){
        this.info = manifest;
    }

    public void registerHookListener(Class<?> classRef){
        if(hookListeners.contains(classRef)) return; //prevent duplicates
        hookListeners.add(classRef);

        //This caches all the methods annotated with @Hook so that hook method lookup later on is faster.
        //If @Hook(name="something") we use the name value of the annotation, otherwise we use the method's actual name.
        for(Class HookListener : hookListeners){
            for(Method method : HookListener.getDeclaredMethods()){
                if(method.isAnnotationPresent(Hook.class)){
                    if(method.getAnnotation(Hook.class).name().isEmpty()){
                        if(hooks.containsKey(method.getName())) continue;
                        hooks.put(method.getName(), method);
                    }else{
                        if(hooks.containsKey(method.getAnnotation(Hook.class).name())) continue;
                        hooks.put(method.getAnnotation(Hook.class).name(), method);
                    }
                }
            }
        }

        Chimera.logMessage(this.getID() + " registered hook listener: "+classRef.getName());
    }

    protected Method getHook(String hookName){
        if(this.implementsHook(hookName)) return hooks.get(hookName);
        return null;
    }

    protected boolean implementsHook(String hookName){
        return hooks.containsKey(hookName);
    }

    protected boolean hasHookListeners(){
        return !hookListeners.isEmpty();
    }

    protected List<Class<?>> getHookListeners(){
        return hookListeners;
    }

    protected static boolean validateManifest(Map<String,String> manifest){
        if(!manifest.isEmpty() && manifest.get("main_class") != null && manifest.get("id") != null
                && manifest.get("name") != null  && manifest.get("author") != null  && manifest.get("description") != null){
            return true;
        }
        return false;
    }

    public String getID(){return info.get("id");}
    public String getName(){return info.get("name");}
    public String getAuthor(){return info.get("author");}
    public String getDescription(){return info.get("description");}
    public String getMainClassName(){return info.get("main_class");}
}