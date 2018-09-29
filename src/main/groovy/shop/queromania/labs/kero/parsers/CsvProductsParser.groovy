package shop.queromania.labs.kero.parsers

import groovy.json.JsonBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import shop.queromania.labs.kero.Utils

class CsvProductsParser {

    final Map<String, Integer> indexes = [
            uniqueUrl: 0, name: 1, categories: 2, size: 4, color: 6, price: 9, discountPrice: 10, sku: 16,
            display  : 18, description: 20, tags: 21, seoTitle: 22, seoDescription: 23
    ]

    static main(args) {
        /** TODO refactor to accept -i and -o */
        new File('outputs/products-exported.json').write(new JsonBuilder(
                new CsvProductsParser()
                        .parse('inputs/exported/produtos-export-nuvemshop-20180826.csv')
                        .values()
                        .collectEntries(toProduct)
        ).toPrettyString(), 'UTF-8')
    }

    Map parse(String input) {
        def products = [:] as Map

        CSVParser.parse(
                new FileReader(input),
                CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
        ).each { CSVRecord line ->
            def uniqueUrl = line.get(indexes.uniqueUrl)
            def sku = line.get(indexes.sku)?.padLeft(6, '0')
            def id = "$uniqueUrl-$sku".toString()

            def tags = line.get(indexes.tags)?.split(/,/)?.collect(Utils.normalize) ?: []
            def discountPriceStr = line.get(indexes.discountPrice).trim()

            def product = (id in products.keySet()) ?
                    addVariations(
                            products[id] as Map,
                            line.get(indexes.color),
                            line.get(indexes.size)
                    ) :
                    [
                            id             : id,
                            uniqueUrl      : uniqueUrl.trim(),
                            name           : line.get(indexes.name)?.trim(),
                            categories     : line.get(indexes.categories)?.split(/,/)
                                    ?.collect { it.trim() } ?: [],
                            sizes          : [line.get(indexes.size)?.trim()],
                            colors         : [line.get(indexes.color)?.trim()],
                            price          : Utils.asNumber(line.get(indexes.price)),
                            discountPrice  : discountPriceStr ? Utils.asNumber(discountPriceStr) : null,
                            sku            : sku,
                            display        : line.get(indexes.display)?.trim()?.toUpperCase() == 'SIM',
                            descriptionHtml: line.get(indexes.description)
                                    ?.replaceAll(/[\r\t\n\s]+/, ' '),
                            tags           : tags,
                            seo            : [
                                    title      : line.get(indexes.seoTitle),
                                    description: line.get(indexes.seoDescription)
                            ]
                    ]
            products << [(id): product]
        }

        return products
    }

    static toProduct = {
        [it.id, [
                id         : it.id,
                uniqueUrl  : it.uniqueUrl,
                sku        : it.sku,
                name       : it.name,
                variants   : [sizes: it.sizes, colors: it.colors],
                taxonomy   : [custom: it.categories],
                description: [custom: it.descriptionHtml],
                price      : [
                        base       : it.price,
                        promotional: [global: it.discountPrice]
                ],
                display    : [global: it.display],
                seo        : [
                        title      : it.seo.title,
                        description: it.seo.description
                ]
        ]]
    }

    static private Map addVariations(Map product, String color, String size) {
        def colors = (product.colors as List) ?: []
        if (!(color in colors)) {
            product << [colors: colors + [color]]
        }

        def sizes = (product.sizes as List) ?: []
        if (!(size in sizes)) {
            product << [sizes: sizes + [size]]
        }

        return product
    }
}
