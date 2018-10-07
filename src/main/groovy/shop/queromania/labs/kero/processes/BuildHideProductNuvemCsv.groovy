package shop.queromania.labs.kero.processes

import shop.queromania.labs.kero.orm.ProductsListOperator
import shop.queromania.labs.kero.parsers.CampaignProductsCsvParser

class BuildHideProductNuvemCsv {

    static run(String inputFilePath, String outputFileName) {
        def pricesBySku = CampaignProductsCsvParser.parseFromFile(inputFilePath)
        def products = new ProductsListOperator().list() as List<Map>

        new File("outputs/${outputFileName}")
                .write(toCsvLines(products.findAll { !(it.sku in pricesBySku.keySet()) }))

    }

    static String toCsvLines(List<Map> products) {
        'Identificador URL,SKU,Exibir na loja\n' +
                (products.collect { Map product ->
                    // the number of variants must match on the platform
                    GroovyCollections.combinations(product.variants.sizes, product.variants.colors).collect {
                        [product.uniqueUrl, product.sku, 'NÃO'].join(',')
                    }.join('\n')
                }.join('\n'))
    }

    static main(args) {
        run('inputs/prices/camp17-18-19_01-19.csv', 'hide-products_export-20181007.csv')
        println('Done!')
    }

}
