package shop.queromania.labs.kero

import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import java.util.regex.Matcher

class Main {

    static final TOKEN = System.getenv('TOKEN')
    static final REQUEST_PROPERTIES = [
            Accept      : 'utf-8',
            Connection  : 'keep-alive',
            Cookie      : "__cfduid=$TOKEN".toString(),
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) ' +
                    'Chrome/67.0.3396.99 Safari/537.36'
    ]

    static main(args) {
        def ids = ['298095', '000115', '052114', '093013', '000118', '093114', '063114', '267093', '211093', '267134',
                   '211134', '298134', '020128', '085128', '267094', '212094']

        def products = ids.collect { id ->
            def content = "http://demillus.vestemuitomelhor.com.br/?s=$id".toURL()
                    .getText(requestProperties: REQUEST_PROPERTIES)
                    .replaceAll(/[\r\n\t]/, '')

            content.findAll(~'http://demillus.vestemuitomelhor.com.br/pecas/.+?/')
        }.flatten().unique().collect { url ->
            def contentNode = Jsoup.connect(url).get().select('div.entry-content').first()
            def descriptionNode = contentNode.select('div.descriptions').first()
            def description = descriptionNode.select('p').first().text()
            [
                    url        : url,
                    title      : contentNode.select('h1.entry-title').first().text(),
                    excerpt    : contentNode.select('p.excerpt').first().text(),
                    description: description,
                    id         : descriptionNode.select('span').first().text().find(~'\\d{6}'),
                    sizes      : getAvailableSizes(description),
                    images     : contentNode.select('div.images').first()
                            .select('a').collect { it.attr('href') },
                    colors     : getAvailableColors(contentNode.select('div.cores'))
            ]
        }

        products.each { println(JsonOutput.prettyPrint(JsonOutput.toJson(it))) }
    }

    static List<String> getAvailableSizes(String description) {
        if (!description) return []

        description = description.toUpperCase()

        def sizeList = { Matcher matcher ->
            println(matcher[0])
            def lower = Integer.parseInt(matcher.group(1))
            def higher = Integer.parseInt(matcher.group(2))
            return (lower..higher).findAll { it % 2 == 0 }.collect { "$it".toString() }
        }

        def matcher = (description =~ /TAM\.: (\d+) A (\d+)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        matcher = (description =~ /TAM\.:(( \w+)+)/)
        if (matcher.size()) {
            println(matcher[0])
            return matcher.group(1).trim().split(/\s+/)
        }

        matcher = (description =~ /\(VESTE \w+ E \w+\)/)
        if (matcher.size()) {
            println(matcher[0])
            return [matcher.group(1), matcher.group(2)]
        }

        matcher = (description =~ /\(VESTE \d+ AO? \d+\)/)
        if (matcher.size()) {
            return sizeList(matcher)
        }

        []
    }

    static List<String> getAvailableColors(Elements colorsNode) {
        if (!colorsNode.size()) return []
        colorsNode.first().select('a.field-color').collect { it.text() }
    }

}