package tech.derbent.config;

//package tech.derbent.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CSpringContext implements ApplicationContextAware {
 private static ApplicationContext ctx;

 @Override
 public void setApplicationContext(ApplicationContext applicationContext) {
     ctx = applicationContext;
 }

 public static <T> T getBean(Class<T> type) {
     return ctx.getBean(type);
 }
}
