package com.ruanchao.annotation_compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class ProxyClass {

    private static final Object SUFFIX = "$$Proxy";
    //定义接口，自动生成的类实现该接口，以便于面向接口编程
    private static final ClassName IPROXY = ClassName.get("com.ruanchao.annotation_api","IProxy");
    /**
     * 类元素
     */
    public TypeElement mTypeElement;
    /**
     * 元素相关的辅助类
     */
    private Elements mElementUtils;

    /**
     * 用于存放注解元素相关信息的对象集合
     */
    Set<FiledViewBinding> mFiledViewBindingSet = new LinkedHashSet<>();

    private static final String INJECT_METHOD = "inject";

    public ProxyClass(TypeElement mTypeElement, Elements mElementUtils) {
        this.mTypeElement = mTypeElement;
        this.mElementUtils = mElementUtils;
    }

    /**
     * 把注解元素的对象加入到当前代理类的集合中
     * @param filedViewBinding
     */
    public void addFiledViewBinding(FiledViewBinding filedViewBinding){
        mFiledViewBindingSet.add(filedViewBinding);
    }

    /**
     * 用于生成代理类
     * @return
     */
    public JavaFile generateProxy(){
        //1.生成public void inject(final T target, View root)方法
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(INJECT_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mTypeElement.asType()), "target", Modifier.FINAL)
                .addParameter(ClassName.get("android.view", "View"), "root");
        //2.在inject方法中，添加我们的findViewById逻辑
        for (FiledViewBinding filedViewBinding : mFiledViewBindingSet){
            injectMethodBuilder.addStatement("target.$N = ($T)(root.findViewById($L))",
                    filedViewBinding.mVariableName,
                    ClassName.get(filedViewBinding.mTypeMirror),
                    filedViewBinding.mResId);
        }

        //3.添加以$$Proxy为后缀的类
        TypeSpec classBuilder = TypeSpec.classBuilder(mTypeElement.getSimpleName().toString() + SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(IPROXY, TypeName.get(mTypeElement.asType())))
                //把inject方法添加到当前类中
                .addMethod(injectMethodBuilder.build())
                .build();

        //4.添加包名
        String packageName = mElementUtils.getPackageOf(mTypeElement).getQualifiedName().toString();

        //5.生成Java文件
        return JavaFile.builder(packageName, classBuilder).build();
    }

/* 要生成的代理类如下格式（BindView）
    public class MainActivity$$Proxy implements IProxy<MainActivity> {
        @Override
        public void inject(final MainActivity target, View root) {

            target.button = (Button)(root.findViewById(R.id.btn));
            target.textView = (TextView)(root.findViewById(R.id.tv));

            View.OnClickListener listener;

            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    target.myClick(view);
                }
            } ;

            (root.findViewById(R.id.btn)).setOnClickListener(listener);
            (root.findViewById(R.id.tv)).setOnClickListener(listener);
        }
    }
    */

}
