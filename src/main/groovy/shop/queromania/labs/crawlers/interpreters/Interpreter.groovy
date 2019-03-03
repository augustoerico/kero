package shop.queromania.labs.crawlers.interpreters

import org.apache.commons.lang3.StringUtils

import java.util.regex.Matcher

class Interpreter {

    static Map interpret(Map product) {
        def prettyDescription = prettify(product.description as String)
        def normalizedDescription = normalize(prettyDescription)
        def paddedSku = (product.sku as String).trim().padLeft(6, '0')
        def normalizedName = normalize(prettify(product.name as String))
                .toLowerCase().replaceAll(/[^0-9^a-z]/, '-')
        def tags = List.of(
                "$normalizedDescription $normalizedName ${normalize(product.taxonomy as String)}".toString()
                        .split(/\s+/)
        )

        def parsedProduct = [
                sku        : paddedSku,
                name       : prettify(product.name as String),
                supplierUrl: product.url,
                id         : "${normalizedName}-${paddedSku}",
                description: prettyDescription,
                excerpt     : prettify(product.excerpt as String),
                variants   : [
                        sizes : getSizesFromDescription(normalizedDescription),
                        colors: product.variants.colors
                ],
                images     : product.imageLinks.collect { [url: it /* TODO add altText */] },
                tags       : tags
        ] as Map

        def categories = Category.getCategories(parsedProduct)
        parsedProduct + [categories: categories]
    }

    static String prettify(String string) {
        return string?.trim()?.replace(/\s{2}/, ' ')
    }

    static normalize = {
        StringUtils.stripAccents(it as String).toLowerCase()
    }

    static List<String> getSizesFromDescription(String description) {
        if (!description) return []

        def processableDescription = normalize(prettify(description)).toUpperCase()

        def sizeList = { Matcher matcher ->
            println(matcher.pattern())
            println(matcher[0])
            def lower = Integer.parseInt(matcher.group(1))
            def higher = Integer.parseInt(matcher.group(2))
            return (lower..higher).findAll { it % 2 == 0 }.collect { "$it".toString() }
        }

        def matcher = (processableDescription =~ /UN \(VESTE (\w+) AO? (\w+)\)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            def min = matcher.group(1)
            def max = matcher.group(2)
            return ["UN ($min - $max)"]
        }

        matcher = (processableDescription =~ /TAM\.: (\d+) A (\d+)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        matcher = (processableDescription =~ /TAMANHOS: (\d+) A (\d+)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        matcher = (processableDescription =~ /TAM\.:(( \w+)+)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            return matcher.group(1).trim().split(/\s+/)
        }

        matcher = (processableDescription =~ /TAMANHOS:(( \w+)+)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            return matcher.group(1).trim().split(/\s+/)
        }

        []
    }

    static main(args) {
        def product = [
                url        : "http://demillus.vestemuitomelhor.com.br/pecas/colecao-idylle-2/#main",
                sku        : "053577",
                name       : "Biquini Idylle",
                description: "Todo em renda elástica com transparência. Tam.: PE ME GR EG.",
                excerpt    : "Sofisticada! Em exclusiva renda elástica com transparência",
                imageLinks : [
                        "http://demillus.vestemuitomelhor.com.br/wp-content/uploads/2019/02/63577-S-Idylle-53577-Biquini-Idylle-27-2.jpg",
                        "http://demillus.vestemuitomelhor.com.br/wp-content/uploads/2019/02/063577-053577-20-2.jpg",
                        "http://demillus.vestemuitomelhor.com.br/wp-content/uploads/2019/02/63577-53577-27-2.jpg",
                        "http://demillus.vestemuitomelhor.com.br/wp-content/uploads/2019/02/067577-057577-11-2.jpg",
                        "http://demillus.vestemuitomelhor.com.br/wp-content/uploads/2019/02/67577-S-Impression-57577-C-Classica-Impression-27-2.jpg"
                ],
                variants   : [
                        colors: ["Preto 27", "Lilás Místico 44", "Branco 20"]
                ],
                taxonomy   : "Calcinhas"
        ]

        println(interpret(product))
    }

}
