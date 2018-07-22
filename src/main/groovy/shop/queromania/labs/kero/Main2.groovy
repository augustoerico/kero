package shop.queromania.labs.kero

import com.opencsv.CSVReader
import groovy.json.JsonBuilder

class Main2 {

    static main(args) {

        def reader = new CSVReader(new FileReader('produtos.csv'))
        def products = [:]
        reader.each {
            def uniqueUrl = it[0]
            if ((!uniqueUrl in products.keySet())) {
                def product = [
                        uniqueUrl  : uniqueUrl,
                        categories : it[2],
                        description: it[20],
                        tags       : it[21].split(/,/).collect { it.trim() },
                        seo        : [
                                title      : it[22],
                                description: it[23]
                        ]
                ]
                products[uniqueUrl] = product
            }
        }

        new File('products_old.json').write(new JsonBuilder(products).toPrettyString())

    }

}
