package org.springframework.beans.factory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class BeanFactory {
    private Map<String, Object> singletons = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public Object getBean(String beanName) {
        return singletons.get(beanName);
    }

    public void addPostProcessor(BeanPostProcessor beanPostProcessor){
        beanPostProcessorList.add(beanPostProcessor);
    }

    public void instantiate(String basePackage) {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        String path = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.toURI());
                for (File classFile : file.listFiles()) {
                    // Поставилии фильтр для сканирования файлов только типа типа class, избавились от NullPointerException
                    if (!classFile.getName().endsWith(".class"))
                        continue;
                    // Будем мапить классы  помеченные только  аннотацией @Component
                    Class classObject = Class.forName(basePackage + "." + getFileName(classFile));
                    if (classObject.isAnnotationPresent(Component.class)) {
                        Object instance = classObject.newInstance();
                        StringBuilder beanName = new StringBuilder(getFileName(classFile).substring(0, 1).toLowerCase() + getFileName(classFile).substring(1));

                        injectBeanName(instance, beanName.toString());
                        injectBeanFactory(instance);
                        initializeBean(instance);


                        singletons.put(beanName.toString(), instance);
                    }
                }
            }

            populateProperties();

        } catch (IOException | URISyntaxException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    private void initializeBean(Object instance) {
        if(instance instanceof  InitializingBean)
            ((InitializingBean) instance).afterPropertiesSet();
    }

    private void injectBeanFactory(Object instance) {
        if(instance instanceof  BeanFactoryAware)
            ((BeanFactoryAware) instance).setBeanFactory(this);
    }

    private String getFileName(File classFile) throws NullPointerException {
        // Не будем плодить обьекты исподьзуем билдер
        StringBuilder fileName = new StringBuilder(classFile.getName());
        return fileName.replace(0, fileName.length(), fileName.toString().substring(0, fileName.toString().lastIndexOf("."))).toString();
    }

    private void populateProperties() {
        System.out.println("==populateProperties==");

        // Бегаем про бинам и ищем поля отмеченные Autowire
        for (Object externalBean : singletons.values()) {
            for (Field field : externalBean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {

                    // Являеться ли поле отмеченное Autowire компонентом
                    for (Object innerBean : singletons.values()) {
                        if (innerBean.getClass().equals(field.getType())) {
                            inject(innerBean, field, externalBean);
                            System.out.println("Есть соответствующая зависимость. Bean " + externalBean.getClass() + "  ; Type Of Field: " + field.getType());
                        }
                    }
                }
            }
        }
    }


    private void inject(Object internalBean, Field field, Object externalBean) {
        String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            Method setter = externalBean.getClass().getMethod(setterName, internalBean.getClass());
            System.out.println("Setter method " + setter);
            setter.invoke(externalBean, internalBean);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private void injectBeanName(Object instance, String beanName) {
        if (instance instanceof BeanNameAware)
            ((BeanNameAware) instance).setBeanName(beanName.toString());
    }
}


