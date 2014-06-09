package org.yats.trading;


// todo: introduce class ProductStaticData that describes details of product. get data from db on subscription

import org.yats.common.UniqueId;

public class Product {



//    public static ProductNULL NULL = new ProductNULL();

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ",symbol='" + symbol + '\'' +
                ",exchange='" + exchange + '\'' +
                ",bloombergId='" + bloombergId + '\'' +
                ",name='" + name + '\'' +
                ",route='" + route + '\'' +
                ",unitId='" + unitId + '\'' +
                '}';
    }


//    public boolean isSameAs(Product other) {
//        return other.hasProductId(productId);
//    }

    public boolean hasProductId(String pid) {
        return pid.compareTo(productId) == 0;
    }

    public String getProductId() {
        return productId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getBloombergId() {
        return bloombergId;
    }

    public String getName() {
        return name;
    }

    public String getRoute() {
        return route;
    }

    public String toStringCSV() {
        return ""+productId+","+symbol+","+exchange+","+bloombergId+","+name+","+route+","+unitId;
    }

    public Product withProductId(String p) {
        productId = p;
        return this;
    }

    public Product withSymbol(String s) {
        symbol=s;
        return this;
    }

    public Product withExchange(String e) {
        exchange=e;
        return this;
    }

    public Product withBloombergId(String s) {
        bloombergId = s;
        return this;
    }

    public Product withName(String s) {
        name = s;
        return this;
    }

    public Product withRoute(String s) {
        route = s;
        return this;
    }

    public Product withUnitId(String s) {
        unitId = s;
        return this;
    }

    public Product(String productId, String symbol, String exchange) {
        this.productId = productId;
        this.symbol = symbol;
        this.exchange = exchange;
    }

    public Product() {
        productId = new UniqueId().toString();
    }

    private String productId;
    private String symbol;
    private String exchange;
    private String bloombergId;
    private String name;
    private String route;
    private String unitId;

    public boolean isRoute(String r) {
        return (route.compareTo(r)==0);
    }

//    private static class ProductNULL extends Product {
//        public String getProductId() { throw new RuntimeException("This is null object!");}
//        public String getSymbol() {
//            throw new RuntimeException("This is null object!");
//        }
//        public String getExchange() {
//            throw new RuntimeException("This is null object!");
//        }
//        private ProductNULL() { super("NULL","NULL","NULL");}
//    }

} // class
