package com.ruanchao.annotation_compiler.model;

import com.ruanchao.annotations.BindView;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 对注解中的元素封装成对象，便于后期生成对应的Java文件
 */
public class FiledViewBinding {

    /**
     * 注解元素
     */
    VariableElement mElement;

    /**
     * 元素id
     */
    int mResId;

    /**
     * 变量名
     */
    String mVariableName;

    /**
     * 变量类型
     */
    TypeMirror mTypeMirror;

    public FiledViewBinding(Element element) {
        this.mElement = (VariableElement) element;
        BindView annotation = element.getAnnotation(BindView.class);
        mResId = annotation.value();
        mVariableName = element.getSimpleName().toString();
        mTypeMirror = element.asType();
    }

    public VariableElement getmElement() {
        return mElement;
    }

    public int getmResId() {
        return mResId;
    }

    public String getmVariableName() {
        return mVariableName;
    }

    public TypeMirror getmTypeMirror() {
        return mTypeMirror;
    }
}
