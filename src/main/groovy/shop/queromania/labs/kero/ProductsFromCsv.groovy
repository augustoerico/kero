package shop.queromania.labs.kero

import groovy.json.JsonBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.nio.charset.Charset

class ProductsFromCsv {

    static main(args) {
        def products = get()
        new File('products_old.json').write(new JsonBuilder(products).toPrettyString(), 'UTF-8')
    }

    static Map get() {
        def products = [:]
        CSVParser.parse(
                new File('produtos.csv'),
                Charset.forName('ISO-8859-1'),
                CSVFormat.DEFAULT
        ).each { CSVRecord line ->
            println(line)
            def uniqueUrl = line.get(0)
            if (!(uniqueUrl in products.keySet())) {
                def product = [
                        uniqueUrl  : uniqueUrl,
                        categories : line.get(2).split(/,/).collect { it.trim() },
                        description: line.get(20),
                        tags       : line.get(21).split(/,/).collect { it.trim() },
                        seo        : [
                                title      : line.get(22),
                                description: line.get(23)
                        ]
                ]
                products[uniqueUrl] = product
            }
        }
        products
    }

}
