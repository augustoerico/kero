package shop.queromania.labs.kero

import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringEscapeUtils

class ProductsCsvExporter {

//    def path = 'outputs/products-merged.json'

    static main(args) {
        export('outputs/products_fetched-20180827.json')
    }

    static export(String input) {
        def products = []
        new JsonSlurper().parse(new File(input)).each { k, v ->
            def lines = productToCsv(v as Map)
            products << lines
        }
        def header = 'Identificador URL,Nome,Categorias,' +
                'Nome da variação 1,Valor da variação 1,Nome da variação 2,Valor da variação 2,' +
                'Preço,Preço promocional,' +
                'Peso,Altura,Largura,Comprimento,' +
                'Estoque,SKU,Código de barras,' +
                'Exibir na loja,Frete gratis,' +
                'Descrição,Tags,Título para SEO,Descrição para SEO\n'
        new File('outputs/products-import.csv').write(header + products.flatten().join('\n'))
    }

    // FIXME use a proper CSV Builder
    static List productToCsv(Map product) {
        GroovyCollections.combinations(product.variants.sizes, product.variants.colors).collect {
            def item = it as List
            def description = (
                    product.description.custom ?: formatDescription(product.description.provider as String)
            ).replaceAll(/"/, '""')

            def categories = (product.taxonomy?.custom ?:
                    product.taxonomy?.provider) ?: []
            [
                    product.uniqueUrl,
                    product.name,
                    "\"${categories.join(',')}\"",
                    'Tamanho',
                    item[0],
                    'Cor',
                    (item[1] as String)?.toLowerCase() ?: '',
                    product.price.base ?: '',
                    product.price.promotional.global ?: '',
                    // Weight, Dimensions, Stock
                    0, // peso TODO
                    0, // altura TODO
                    0, // largura TODO
                    0, // comprimento
                    '',
                    // SKU
                    "\"${product.sku}\"",
                    '',
                    product.display.global ? 'SIM' : 'NÃO',
                    'NÃO',
                    "\"${description}\"",
                    // SEO
                    "\"${(product.tags ?: []).join(',')}\"",
                    product.seo?.title ?: product.name,
                    "\"${product.seo?.description ?: ''}\""
            ].join(',')
        }
    }

    static String formatDescription(String description) {
        description = StringEscapeUtils.escapeHtml4(description)
        def attentionTitle = StringEscapeUtils.escapeHtml4('ATENÇÃO')
        def attentionNote = StringEscapeUtils.escapeHtml4('A confecção da DeMillus é pequena. Consulte a tabela ' +
                'abaixo para saber suas medidas e evitar trocas.')
        "<p>$description</p>" +
                "<p>&nbsp;</p>" +
                "<p><span style=\"color:#FF0000;\"><strong>$attentionTitle</strong></span></p>" +
                "<p><strong>$attentionNote</strong><p>" +
                "<p>&nbsp;</p>"
        // TODO add more key-specific info: tables, images and such
    }
}
