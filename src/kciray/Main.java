package kciray;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.Service;

@Service
public class Main {
    public static void main(String[] args) {
       BeanFactory beanFactory = new BeanFactory();
       beanFactory.instantiate("kciray");
       ProductService productService = (ProductService) beanFactory.getBean("productService");
       PromotionService promotionService = (PromotionService) beanFactory.getBean("promotionService");
       System.out.println(promotionService.getBeanName());
    }
}
