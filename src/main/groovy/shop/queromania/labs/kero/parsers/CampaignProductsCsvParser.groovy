package shop.queromania.labs.kero.parsers

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

class CampaignProductsCsvParser {

    static Map<String, Integer> indexes = [
            sku: 0, price: 1, discountPrice: 2
    ]

    static Map parseFromFile(String inputFilePath) {

        def lines = CSVParser.parse(
                new FileReader(inputFilePath),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
        ).collect { CSVRecord line ->
            def properties = [
                    sku  : line.get(indexes.sku).padLeft(6, '0').toUpperCase(),
                    price: Float.parseFloat(line.get(indexes.price).replace(',', '.')),
            ]
            if (line.get(indexes.discountPrice)) {
                properties + ([discountPrice: Float.parseFloat(
                        line.get(indexes.discountPrice).replace(',', '.')
                )] as Map)
            } else {
                properties
            }
        }

        return parse(lines)
    }

    static Map parse(List properties) {
        Map products = [:]
        properties.each(parse.curry(products))
        products
    }

    static parse = { products, properties ->
        def sku = properties.sku as String
        def price = properties.price
        def discountPrice = properties.discountPrice

        if (sku in products.keySet()) {
            def priceObj = (products[sku] as Map).price as Map
            if (price != priceObj.base || discountPrice != priceObj.promotional?.global) {
                throw new RuntimeException("Price differs for sku = ${sku}")
            }
        } else {
            if (discountPrice) {
                products[sku] = [
                        sku  : sku,
                        price: [base: price, promotional: [global: discountPrice]]
                ]
            } else {
                products[sku] = [
                        sku  : sku,
                        price: [base: price]
                ]
            }
        }
    }
}
