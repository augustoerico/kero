package shop.queromania.labs.kero

import groovy.json.JsonBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import java.util.regex.Matcher

class ProductsFetcher {

    def path = "inputs/prices/untitled.csv"

    static main(args) {
        new File('outputs/products-fetched.json').write(new JsonBuilder(
                new ProductsFetcher().fetch()
        ).toPrettyString(), 'UTF-8')
    }

    Map pricesBySku() {

        CSVParser.parse(
                new FileReader(this.path),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
        ).collect { CSVRecord line ->
            def sku = line.get(0).padLeft(6, '0')
            def price = Utils.asNumber(line.get(1))
            def discountPrice = line.get(2) ? Utils.asNumber(line.get(2)) : null

            [sku, [price: price, discountPrice: discountPrice]]
        }.collectEntries()
    }

    Map fetch() {
        def pricesBySku = this.pricesBySku()

        pricesBySku.keySet().collect {
            def url = "http://demillus.vestemuitomelhor.com.br/?s=$it"
            println("Querying url = $url")
            def pageNode = Jsoup.connect(url).get()
            def links = pageNode.select('div.list-item')?.collect {
                it.select('a[href*=pecas]')?.collect { it.attr('href') }
            }
            if (!links) {
                println(it)
            }
            links
        }.flatten().unique().findAll { url ->
            println("Getting info from ${url}")
            Jsoup.connect(url as String).get() != null
        }.collect { url ->
            def pageNode = Jsoup.connect(url as String).get()
            def contentNode = pageNode.select('div.entry-content')?.first()
            def descriptionNode = contentNode.select('div.descriptions')?.first()

            def providerDescription = descriptionNode?.select('p')?.first()?.text()?.trim()

            [
                    sku        : descriptionNode.select('span').first().text().find(~'\\w\\d{5}'),
                    url        : url,
                    name       : contentNode?.select('h1.entry-title')?.first()?.text()?.trim(),
                    description: [
                            excerpt : contentNode?.select('p.excerpt')?.first()?.text()?.trim(),
                            provider: providerDescription
                    ],
                    imageLinks : contentNode?.select('div.images')?.first()
                            ?.select('a')?.collect { it.attr('href') },
                    variants   : [
                            colors: getAvailableColors(contentNode?.select('div.cores')),
                            sizes : getAvailableSizes(providerDescription)
                    ],
                    taxonomy   : [
                            provider: pageNode?.select('ul#menu-principal')?.first()
                                    ?.select('li.current-menu-parent > a')?.text()
                    ]
            ]
        }.collect {
            def description = it.description as Map
            def taxonomy = it.taxonomy as Map
            (it as Map) + [normalized: [
                    name       : Utils.normalize(it.name),
                    description: Utils.normalize(description.provider),
                    excerpt    : Utils.normalize(description.excerpt),
                    category   : Utils.normalize(taxonomy.provider)
            ]]
        }.collect {
            def uniqueUrl = (it.normalized as Map).name.replaceAll(/[^a-z^0-9]/, '-')
            def id = "$uniqueUrl-${it.sku}".toString()
            (it as Map) + [
                    id       : id,
                    uniqueUrl: uniqueUrl,
                    price    : [
                            base       : (pricesBySku.get(it.sku) as Map)?.price,
                            promotional: [global: (pricesBySku.get(it.sku) as Map)?.discountPrice]
                    ],
                    tags     : getTags(it)
            ]
        }.collect {
            def taxonomy = (it.taxonomy as Map) +
                    [custom: Category.getCategories(it).collect { it.toString() }]
            (it as Map) + [taxonomy: taxonomy]
        }.collect {
            (it as Map) + [display: [global:  it.price.base != null]]
        }.collect {
            [it.id, it]
        }.collectEntries()
    }

    static List<String> getAvailableSizes(String description) {
        if (!description) return []

        description = description.toUpperCase()

        def sizeList = { Matcher matcher ->
            println(matcher.pattern())
            println(matcher[0])
            def lower = Integer.parseInt(matcher.group(1))
            def higher = Integer.parseInt(matcher.group(2))
            return (lower..higher).findAll { it % 2 == 0 }.collect { "$it".toString() }
        }

        def matcher = (description =~ /UN \(VESTE (\w+) AO? (\w+)\)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            def min = matcher.group(1)
            def max = matcher.group(2)
            return ["UN ($min - $max)"]
        }

        matcher = (description =~ /TAM\.: (\d+) A (\d+)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        matcher = (description =~ /TAMANHOS: (\d+) A (\d+)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        matcher = (description =~ /TAM\.:(( \w+)+)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            return matcher.group(1).trim().split(/\s+/)
        }

        matcher = (description =~ /TAMANHOS:(( \w+)+)/)
        if (matcher.size()) {
            println(matcher.pattern())
            println(matcher[0])
            return matcher.group(1).trim().split(/\s+/)
        }

        ['']
    }

    static List<String> getAvailableColors(Elements colorsNode) {
        if (!colorsNode.size()) return []
        colorsNode.first().select('a.field-color').collect { it.text() }
    }

    static List getTags(Map product) {
        [
                (product.normalized.name as String).split(/\s+/),
                (product.normalized.category as String).split(/\s+/)
        ].flatten().findAll { it && (it =~ /\w+/).size() }.unique()
    }
}