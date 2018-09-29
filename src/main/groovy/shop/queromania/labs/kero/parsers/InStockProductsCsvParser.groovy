package shop.queromania.labs.kero.parsers

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

class InStockProductsCsvParser {

    static Map<String, Integer> indexes = [
            sku: 0, color: 1, size: 2, price: 3
    ]

    static Map parseFromFile(String inputFilePath) {

        Map products = [:]

        def lines = CSVParser.parse(
                new FileReader(inputFilePath),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
        ).collect { CSVRecord line ->
            [
                    sku  : line.get(indexes.sku).padLeft(6, '0'),
                    color: line.get(indexes.color),
                    size : line.get(indexes.size),
                    price: Float.parseFloat(line.get(indexes.price))
            ]
        }

        return parse(lines)
    }

    static Map parse(List properties) {
        Map products = [:]
        properties.each(parse.curry(products))
        products
    }

    static parse = { products, properties ->
        def sku = (properties.sku as String).toUpperCase()
        def color = (properties.color as String).toLowerCase()
        def size = (properties.size as String).toUpperCase()
        def price = properties.price

        if (sku in products.keySet()) {
            def product = products[sku] as Map

            if (price != product.price.base) {
                throw new RuntimeException("Price differs for sku = ${sku}")
            }

            def found = false
            for (v in product.variants) {
                if (color in v.colors) {
                    found = true
                    v.sizes << size
                } else if (size in v.sizes) {
                    found = true
                    v.colors << color
                }
            }

            if (!found) {
                product.variants << [sizes: [size], colors: [color]]
            }
        } else {
            products[sku] = [
                    sku     : sku,
                    variants: [[colors: [color], sizes: [size]]],
                    price   : [base: price]
            ]
        }
    }

}
