package com.feng.module.binding.annotation;

import com.feng.module.binding.core.TaskBindingPoll;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * TaskBinding和TaskHandler注解扫描处理器
 * 1. 扫描 Bean类和方法上的 @TaskBinding 注解
 * 2. 类上注解了 @TaskBinding 则扫描方法上的@TaskHandler 注解
 * 2. 封装元数据并注册到全局方法绑定中
 *
 * @author zc
 * Version 1.0
 * Date 2025/03/11 22:07
 */

public class TaskBindingAnnotationBeanPostProcessor implements BeanPostProcessor {

    // 避免同一个类重复扫描
    private final ConcurrentMap<Class<?>, TypeMetadata> typeCache = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        // 获取 Spring 代理对象（AOP 代理）背后的「原始目标类」
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        // 获取封装的元数据
        final TypeMetadata metadata = this.typeCache.computeIfAbsent(targetClass, this::buildMetadata);
        // 处理Method上直接绑定的 @TaskBinding注解的方法信息，添加到方法绑定
        for (BindingMethod lm : metadata.bindingMethods) {
            for (TaskBinding taskBinding : lm.annotations) {
                processMethodBinding(taskBinding, lm.method, bean, beanName);
            }
        }
        // 处理类上绑定了@TaskBinding注解，并且方法上绑定了@TaskHandler注解的方法信息， 添加到方法绑定
        if (metadata.handlerMethods.length > 0) {
            processMultiMethodBinding(metadata.classAnnotations, metadata.handlerMethods, bean, beanName);
        }
        return bean;
    }

    /**
     * 通过类对象构建绑定了@TaskBinding注解和@TaskHandler注解的信息
     *
     * @param targetClass 目标类
     * @return TypeMetadata
     */
    private TypeMetadata buildMetadata(Class<?> targetClass) {
        // 获取类上绑定的@TaskBinding注解信息
        List<TaskBinding> classLevelBindings = findBindingAnnotations(targetClass);
        final boolean hasClassLevelBindings = !classLevelBindings.isEmpty();
        // 存储@TaskBinding直接和方法绑定的信息
        final List<BindingMethod> methods = new ArrayList<>();
        // 存储@TaskHandler和方法绑定的信息
        final List<HandlerMethod> multiMethods = new ArrayList<>();
        // 遍历类实例中的方法进行处理
        ReflectionUtils.doWithMethods(targetClass, method -> {
            // 获取方法上绑定@TaskBinding注解
            List<TaskBinding> bindingAnnotations = findBindingAnnotations(method);
            if (!bindingAnnotations.isEmpty()) {
                methods.add(new BindingMethod(method,
                        bindingAnnotations.toArray(new TaskBinding[0])));
            }
            // 获取方法上绑定@TaskHandler注解
            if (hasClassLevelBindings) {
                List<TaskHandler> rabbitHandler = findHandlerAnnotations(method);
                if (rabbitHandler != null && !rabbitHandler.isEmpty()) {
                    multiMethods.add(new HandlerMethod(method, rabbitHandler.toArray(new TaskHandler[0])));
                }
            }
        }, ReflectionUtils.USER_DECLARED_METHODS
                .and(meth -> !meth.getDeclaringClass().getName().contains("$MockitoMock$")));
        if (methods.isEmpty() && multiMethods.isEmpty()) {
            return TypeMetadata.EMPTY;
        }
        return new TypeMetadata(
                methods.toArray(new BindingMethod[0]),
                multiMethods.toArray(multiMethods.toArray(new HandlerMethod[0])),
                classLevelBindings.toArray(new TaskBinding[0]));
    }

    /**
     * 通过类或方法，寻找绑定的@TaskBinding的信息
     *
     * @param element 类或方法
     * @return TaskBinding信息列表
     */
    private List<TaskBinding> findBindingAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .stream(TaskBinding.class)// 筛选 @TaskBinding注解
                .filter(tma -> {
                    Object source = tma.getSource();
                    String name = "";
                    if (source instanceof Class<?>) {
                        name = ((Class<?>) source).getName();
                    } else if (source instanceof Method) {
                        name = ((Method) source).getDeclaringClass().getName();
                    }
                    return !name.contains("$MockitoMock$");
                })
                .map(MergedAnnotation::synthesize)// 转为@TaskBinding实例
                .collect(Collectors.toList());
    }

    /**
     * 通过方法，寻找绑定的@TaskHandler的信息
     *
     * @param method 方法
     * @return TaskHandler信息列表
     */
    private List<TaskHandler> findHandlerAnnotations(Method method) {
        // 1. 先排除 Mockito Mock 方法
        String className = method.getDeclaringClass().getName();
        if (className.contains("$MockitoMock$")) {
            return null; // 直接返回空，跳过后续扫描
        }
        // 2. 扫描方法上的所有 @TaskHandler 注解（支持重复标注、层级扫描）
        return MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .stream(TaskHandler.class) // 筛选 @TaskHandler注解
                .map(MergedAnnotation::synthesize) // 转为@TaskHandler实例
                .collect(Collectors.toList());
    }

    protected void processMethodBinding(TaskBinding taskBinding, Method method, Object bean,
                                        String beanName) {
        Method methodToUse = checkProxy(method, bean);
        processBinding(methodToUse, taskBinding, null, bean, beanName);
    }


    private void processMultiMethodBinding(TaskBinding[] classLevelBindings, HandlerMethod[] multiMethods,
                                           Object bean, String beanName) {
        for (HandlerMethod handlerMethod : multiMethods) {
            Method checked = checkProxy(handlerMethod.method, bean);
            for (TaskBinding classLevelBinding : classLevelBindings) {
                processBinding(checked, classLevelBinding, handlerMethod.annotations, bean, beanName);
            }
        }
    }

    private Method checkProxy(Method methodArg, Object bean) {
        Method method = methodArg;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // 仅处理 JDK 动态代理的 Bean，CGLIB 代理/普通 Bean 直接返回原始 method
                method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                // 如果代理类中没找到，遍历代理类实现的所有接口，找匹配的方法
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iFace : proxiedInterfaces) {
                    try {
                        method = iFace.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    } catch (@SuppressWarnings("unused") NoSuchMethodException ignored) {
                        // 没有该方法，忽略异常，继续遍历下一个接口
                    }
                }
            } catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            } catch (NoSuchMethodException ex) {
                // 目标类上有 @TaskBinding 注解的方法，但接口中没有该方法
                throw new IllegalStateException(String.format(
                        "@TaskBinding method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for a bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()), ex);
            }
        }
        return method;
    }

    /**
     * 将注解内容和方法进行绑定
     *
     * @param method      绑定的方法
     * @param taskBinding 绑定注解
     * @param handlers    绑定的多个Handler
     * @param bean        对象bean
     * @param beanName    对象bean名字
     */
    protected void processBinding(Method method,
                                  TaskBinding taskBinding, TaskHandler[] handlers, Object bean, String beanName) {
        if (handlers == null || handlers.length == 0) {
            TaskBindingPoll.getInstance().setTaskBinding(taskBinding, null, method, bean, beanName);
        } else {
            Arrays.stream(handlers).forEach(handler -> {
                TaskBindingPoll.getInstance().setTaskBinding(taskBinding, handler, method, bean, beanName);
            });
        }
    }

    /**
     * TaskBinding元数据
     * {@link TaskBinding} annotations
     */
    private static class TypeMetadata {

        /**
         * {@link TaskBinding}直接绑定的方法数据
         */
        final BindingMethod[] bindingMethods; // NO SONAR

        /**
         * {@link TaskHandler} 绑定的方法数据
         */
        final HandlerMethod[] handlerMethods; // NO SONAR

        /**
         * {@link TaskBinding} 类上绑定的数据.
         */
        final TaskBinding[] classAnnotations; // NO SONAR

        static final TypeMetadata EMPTY = new TypeMetadata();

        private TypeMetadata() {
            this.bindingMethods = new BindingMethod[0];
            this.handlerMethods = new HandlerMethod[0];
            this.classAnnotations = new TaskBinding[0];
        }

        TypeMetadata(BindingMethod[] methods, HandlerMethod[] handlerMethods, TaskBinding[] classLevelBindings) { // NO SONAR
            this.bindingMethods = methods;
            this.handlerMethods = handlerMethods;
            this.classAnnotations = classLevelBindings;
        }

    }

    /**
     * {@link TaskBinding}和方法绑定的数据
     */
    private static class BindingMethod {

        final Method method; // NO SONAR

        final TaskBinding[] annotations; // NO SONAR

        BindingMethod(Method method, TaskBinding[] annotations) { // NO SONAR
            this.method = method;
            this.annotations = annotations;
        }

    }

    /**
     * {@link TaskHandler}和方法绑定的数据
     */
    private static class HandlerMethod {

        final Method method; // NO SONAR

        final TaskHandler[] annotations; // NO SONAR

        HandlerMethod(Method method, TaskHandler[] annotations) { // NO SONAR
            this.method = method;
            this.annotations = annotations;
        }

    }

}