package org.yats.trading;


import org.yats.common.Map;

public class ProfitSnapshot {

    public AccountPosition getProductAccountPosition(String prod1, String account1) {
        throw new RuntimeException("not yet implemented!");
    }

    public void add(ProductAccountProfit p) {
        ProductAccountProfit newProfit = p;
        String key = p.getKey();
        if(profitMap.containsKey(key)) {
            ProductAccountProfit oldProfit = profitMap.get(key);
            newProfit=oldProfit.add(newProfit);
        }
        profitMap.put(key, newProfit);
    }

    public ProfitSnapshot() {
        profitMap = new Map<String, ProductAccountProfit>();
    }

    Map<String, ProductAccountProfit> profitMap;
} // class
