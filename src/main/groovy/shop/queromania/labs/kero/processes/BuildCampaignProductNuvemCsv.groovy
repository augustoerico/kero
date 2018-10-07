package shop.queromania.labs.kero.processes

import shop.queromania.labs.kero.orm.ProductsListOperator
import shop.queromania.labs.kero.parsers.CampaignProductsCsvParser

class BuildCampaignProductNuvemCsv {

    static ProductsListOperator listOperator

    static run(String inputFilePath) {

        def pricesBySku = CampaignProductsCsvParser.parseFromFile(inputFilePath)
        def products = pricesBySku.collect(
                buildProduct.curry(listOperator.list())
        )
    }

    static buildProduct = { List products, String sku, Map priceObj ->
        def product = products[sku] as Map
        if (product) {
            buildExistingProduct(product, priceObj)
        } else {
            fetchAndBuildProduct(sku, priceObj)
        }
    }

    static buildExistingProduct(Map product, Map priceObj) {

    }

    static fetchAndBuildProduct(String sku, Map priceObj) {

    }

    static main(args) {

    }

}
