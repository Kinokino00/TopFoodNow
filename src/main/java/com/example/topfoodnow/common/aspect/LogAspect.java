package com.example.topfoodnow.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    // 定義切入點，匹配所有 controller 套件下的方法
    @Pointcut("execution(* com.example.topfoodnow.controller..*(..))")
    public void controllerPointcut() {}

    // 環繞通知，記錄方法執行時間和狀態
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        logger.info("方法開始執行: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.info("方法成功結束: {}，耗時: {}ms", methodName, duration);
            return result;
        } catch (Throwable e) {
            logger.error("方法拋出錯誤: {}, 錯誤訊息: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
