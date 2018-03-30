package com.fitc.wifihotspot;

import android.content.Context;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.FieldId;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Created by jonro on 19/03/2018.
 */

public final class CallbackMaker {
    private static final String ON_TETHERING_STARTED_METHOD = "onTetheringStarted";
    private static final String ON_TETHERING_STOPPED_METHOD = "onTetheringStopped";
    private final MyOnStartTetheringCallback mAppCallBack;
    Context mContext;
    Class <?> myTetheringCallbackClazz;
    DexMaker dexMaker;

    public CallbackMaker(Context c, MyOnStartTetheringCallback callback){
        mContext = c;
        mAppCallBack = callback;

        Class callbackClass = null;
        try {
            callbackClass = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        TypeId<?> systemCallbackTypeId = TypeId.get(callbackClass);

         dexMaker = new DexMaker();

        // Generate a TetheringCallback class.
        TypeId<?> tetheringCallback = TypeId.get("LTetheringCallback;");

        dexMaker.declare(tetheringCallback, "TetheringCallback.generated", Modifier.PUBLIC, systemCallbackTypeId);

        // Add (Our local normal-Java) callback as a field to the generated callback
        TypeId<MyOnStartTetheringCallback> t = TypeId.get(MyOnStartTetheringCallback.class);
        FieldId<?,?> myCallbackFieldId = tetheringCallback.getField(t, "callback");
        dexMaker.declare(myCallbackFieldId,Modifier.PRIVATE, null);


        generateConstructorWorking(tetheringCallback,systemCallbackTypeId);

        // TODO: Not working
         //generateConstructor(tetheringCallback,systemCallbackTypeId,myCallbackFieldId);
        //  generateCallbackMethod(myTetheringCallback,callbackFieldId, ON_TETHERING_STARTED_METHOD) ;
        //   generateCallbackMethod(myTetheringCallback,callbackFieldId, ON_TETHERING_STOPPED_METHOD) ;

        // Create the dex file and load it.
        File outputDir = mContext.getCodeCacheDir();

        try {
            ClassLoader loader = dexMaker.generateAndLoad(CallbackMaker.class.getClassLoader(),outputDir);
            myTetheringCallbackClazz = loader.loadClass("TetheringCallback");

            // Execute our newly-generated code in-process.
           // myTetheringCallbackClass.getMethod("hello").invoke(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void generateConstructorWorking(TypeId<?> declaringType, TypeId<?> superType){
        final MethodId<?, ?> superConstructor = superType.getConstructor();

        MethodId<?, ?> constructor = declaringType.getConstructor(TypeId.INT);
        Code constructorCode = dexMaker.declare(constructor, Modifier.PUBLIC);
        final Local thisRef = constructorCode.getThis(declaringType);
        constructorCode.invokeDirect(superConstructor, null, thisRef);
        constructorCode.returnVoid();
    }


    /**
     *
     * @param declaringType
     * @param superType
     * @param appCallbackFieldId
     */
    public void generateConstructor(TypeId<?> declaringType, TypeId<?> superType, FieldId<?,?> appCallbackFieldId){
        final MethodId<?, ?> superConstructor = superType.getConstructor();
        TypeId<?> myCallbackTypeId = TypeId.get(MyOnStartTetheringCallback.class);

        MethodId<?, ?> constructor = declaringType.getConstructor(myCallbackTypeId);
        Code constructorCode = dexMaker.declare(constructor, Modifier.PUBLIC);
        final Local thisRef = constructorCode.getThis(declaringType);
        constructorCode.invokeDirect(superConstructor, null, thisRef);
        // constructorCode.iput(appCallbackFieldId, thisRef, param);
        constructorCode.returnVoid();
    }




    public void generateCallbackMethod(TypeId<?> declaringType, FieldId<?,?> appCallbackFieldId, String method){
        // Identify the 'onTetheringStarted()' method on declaringType.
        MethodId onTetheringStarted = declaringType.getMethod(TypeId.VOID, method);

        // Declare that method on the dexMaker. Use the returned Code instance
        // as a builder that we can append instructions to.
        Code code = dexMaker.declare(onTetheringStarted, Modifier.PUBLIC);
        // ref this this instance
        final Local thisRef = code.getThis(declaringType);


        TypeId declaringTypeCallback = appCallbackFieldId.getDeclaringType();
        Local appCallback = code.newLocal(declaringTypeCallback);

        //get reference to the field
        code.iget(appCallbackFieldId,appCallback , thisRef);

        // Call method on field.
        MethodId methodIdInAppCallback =  declaringTypeCallback.getMethod(TypeId.VOID, method, TypeId.VOID);
        code.invokeVirtual(methodIdInAppCallback, null,appCallback , null);

        // return;
        code.returnVoid();

    }


    public Class<?> getCallBackClass(){
        return myTetheringCallbackClazz;
    }




}
