package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //表示匹配 com.sky.mapper 包下的所有方法
    @Pointcut("execution( * com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }


    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("Executing method: " + joinPoint.getSignature().getName());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); //注解对象
        OperationType value = autoFill.value();//操作类型.

        Object[] args = joinPoint.getArgs();
        if(args == null || args.length== 0){
            return;
        }
        Object arg = args[0];//实体对象

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(value == OperationType.INSERT){
            try {
                Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(arg, now);
                setCreateUser.invoke(arg, currentId);
                setUpdateTime.invoke(arg, now);
                setUpdateUser.invoke(arg, currentId);
            } catch (Exception e) {
               e.printStackTrace();
            }

        }
        if(value == OperationType.UPDATE){
            try {
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(arg, now);
                setUpdateUser.invoke(arg, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @After("autoFillPointCut()")
    public void logAfter(JoinPoint joinPoint) {
        log.info("Method executed: " + joinPoint.getSignature().getName());
    }
}
