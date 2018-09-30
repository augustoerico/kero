package shop.queromania.labs.kero.orm

class ProductsListOperator extends ProductsOperator {

    List list() {
        return this.collection.find().asList()
    }

    static main(args) {
        println(new ProductsListOperator().list().toString())
    }

}
