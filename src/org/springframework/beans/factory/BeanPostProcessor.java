package org.springframework.beans.factory;

public interface BeanPostProcessor {
    Object postProcessBeforeInitialization();
    Object postProcessAfterInitialization();
}
