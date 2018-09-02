package kciray;

import org.springframework.beans.factory.Autowired;
import org.springframework.beans.factory.Component;

@Component
public class ProductService {

    @Autowired
    private PromotionService promotionService;

    public PromotionService getPromotionService() {
        return promotionService;
    }

    public void setPromotionService(PromotionService promotionService) {
        this.promotionService = promotionService;
    }



}
