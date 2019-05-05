package com.ruanchao.annotation_compiler;

import com.google.auto.service.AutoService;
import com.ruanchao.annotation_compiler.model.FiledViewBinding;
import com.ruanchao.annotation_compiler.model.ProxyClass;
import com.ruanchao.annotations.BindView;
import com.ruanchao.annotations.OnClick;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class AnnotationProcessor  extends AbstractProcessor {

    private Filer mFiler;//文件相关工具类
    private Elements mElementUtils;//元素相关的工具类
    private Messager mMessager;//日志相关的工具类
    private static final String FIELDS = "fields";
    private static final String PRIVATE = "private";
    private static final String STATIC = "static";
    Map<String, ProxyClass> mProxyMap = new HashMap<>();

    /**
     * 初始化方法，主要用于获取相关的工具类
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();

    }

    /**
     * 处理器的核心方法
     * 用于处理注解，生成Java文件
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("process start  111111");
        mMessager.printMessage(Diagnostic.Kind.NOTE,"rcprocess start  111111");


        //处理被BindView注解的元素
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewElements){
            //注解是否符合规范
            if (!isValid(BindView.class, FIELDS, element)){
                return true;
            }
            //解析注解的元素
            parseBindView(element);
        }

        //根据集合，生成每个代理类文件，依靠Javapoet
        for (ProxyClass proxyClass : mProxyMap.values()){
            try {
                proxyClass.generateProxy().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mProxyMap.clear();

        return true;
    }

    private void parseBindView(Element element) {
        ProxyClass proxyClass = getProxyClass(element);

        //注解元素的封装对象
        FiledViewBinding filedViewBinding = new FiledViewBinding(element);
        proxyClass.addFiledViewBinding(filedViewBinding);

    }

    private ProxyClass getProxyClass(Element element) {
        //创建注解代理类对象，并加入集合中
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();
        ProxyClass proxyClass = mProxyMap.get(qualifiedName);
        if (proxyClass == null){
            proxyClass = new ProxyClass(enclosingElement,mElementUtils);
            mProxyMap.put(qualifiedName, proxyClass);
        }
        return proxyClass;
    }

    /**
     * 判断注解是否符合规范
     * @param bindViewClass
     * @param fields
     * @param element
     * @return
     */
    private boolean isValid(Class<BindView> bindViewClass, String fields, Element element) {
        //获取当前注解的父元素
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        //获取父元素的全名
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        //1.注解所在的类不能是private或者static类型
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)){
            mMessager.printMessage(Diagnostic.Kind.ERROR,"can't contained private or static classes.",element);
            return false;
        }

        //2.父元素必须是类
        if (enclosingElement.getKind() != ElementKind.CLASS){
            mMessager.printMessage(Diagnostic.Kind.ERROR,"father element should is class.",element);
            return false;
        }

        //3.不能在Android框架层注解
        if (qualifiedName.startsWith("android.")) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,"can't android fragment.",element);
            return false;
        }
        //4.不能在java框架层注解
        if (qualifiedName.startsWith("java.")) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,"can't java fragment.",element);
            return false;
        }

        return true;
    }

    /**
     * 指定那些注解需要被注解处理器处理
     * @return
     */
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getName());
        annotations.add(OnClick.class.getName());
        return annotations;
    }

    /**
     * 指定所使用的Java版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
