package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)// 注解所修饰的对象范围,ElementType.METHOD表示该注解只能用于方法
@Retention(RetentionPolicy.RUNTIME)// 注解的保留时间,RetentionPolicy.RUNTIME表示该注解在运行时存在
public @interface AutoFill {
    OperationType value();// value()为注解的成员变量
}
