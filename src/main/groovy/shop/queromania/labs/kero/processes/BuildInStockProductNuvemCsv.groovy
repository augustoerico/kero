package shop.queromania.labs.kero.processes

import shop.queromania.labs.kero.Category as C
import shop.queromania.labs.kero.orm.ProductsGetOperator
import shop.queromania.labs.kero.parsers.InStockProductsCsvParser

class BuildInStockProductNuvemCsv {

    static Map buildProduct(Map product, Map refProduct) {
        def taxonomy = (refProduct.taxonomy as Map)
        taxonomy += [
                custom: taxonomy.custom ?
                        taxonomy.custom << C.PRONTA_ENTREGA.toString() :
                        [C.PRONTA_ENTREGA.toString()]
        ]
        product + refProduct.subMap(['description', 'display', 'id', 'name', 'seo']) + [
                uniqueUrl: refProduct.uniqueUrl + '-in-stock' as String,
                taxonomy : taxonomy
        ]
    }

    static void run(String inputFilePath, String outputFileName) {
        def products = InStockProductsCsvParser.parseFromFile(inputFilePath)
                .collectEntries { k, v -> [k, buildProduct(v as Map, new ProductsGetOperator().get(k as String))] }
        
    }

    static main(args) {




    }

}
