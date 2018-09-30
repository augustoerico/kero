package shop.queromania.labs.kero.orm

import org.bson.Document

class ProductsGetOperator extends ProductsOperator {

    Map get(String sku) {
        def result = this.collection.find(new Document('sku', sku), Map.class).first()
        return result as Map
    }

    static main(args) {
        println(new ProductsGetOperator().get('016368'))
    }

}
