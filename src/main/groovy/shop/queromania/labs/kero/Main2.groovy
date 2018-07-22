package shop.queromania.labs.kero

import com.opencsv.CSVReader
import groovy.json.JsonBuilder

class Main2 {

    static main(args) {

        def reader = new CSVReader(new FileReader('produtos.csv'))
        def products = [:]
        reader.each {
            def uniqueUrl = it[0]
            if (uniqueUrl in products.keySet()) {
                def product = products[uniqueUrl] as Map
                if (it[4] && !(it[4] in product.sizes)) {
                    (product.sizes as List) << it[4].toString()
                }
                if (it[6] && !(it[6] in product.colors)) {
                    (product.colors as List) << it[6].toString()
                }
            } else {
                def sizes = it[4] ? [it[4].toString()] : []
                def colors = it[6] ? [it[6].toString()] : []
                def product = [
                        uniqueUrl  : uniqueUrl,
                        title      : it[1],
                        categories : it[2],
                        sizes      : sizes,
                        colors     : colors,
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
