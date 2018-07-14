package shop.queromania.labs.kero

import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
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
            def contentNode = Jsoup.connect(url as String).get().select('div.entry-content').first()

            def descriptionNode = contentNode.select('div.descriptions').first()
            def description = descriptionNode.select('p').first().text()

            def title = contentNode.select('h1.entry-title').first().text()
            def excerpt = contentNode.select('p.excerpt').first().text()

            [
                    url        : url,
                    title      : title,
                    excerpt    : excerpt,
                    description: description,
                    id         : descriptionNode.select('span').first().text().find(~'\\d{6}'),
                    sizes      : getAvailableSizes(description),
                    images     : contentNode.select('div.images').first()
                            .select('a').collect { it.attr('href') },
                    colors     : getAvailableColors(contentNode.select('div.cores'))
            ]
        }

        exportToCsv(products)
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

//        matcher = (description =~ /(\w+)\s+\(VESTE \w+ E \w+\)/)
//        if (matcher.size()) {
//            println(matcher[0])
//            return [matcher.group(1), matcher.group(2)]
//        }
//
//        matcher = (description =~ /(\w+)\s+\(VESTE \d+ AO? \d+\)/)
//        if (matcher.size()) {
//            return sizeList(matcher)
//        }

        []
    }

    static List<String> getAvailableColors(Elements colorsNode) {
        if (!colorsNode.size()) return []
        colorsNode.first().select('a.field-color').collect { it.text() }
    }

    static void exportToCsv(List products) {
        def file = products.collect {
            productToCsv(it as Map)
        }.flatten().join('\n')
        println(file)
        def header = 'Identificador URL,Nome,Categorias,' +
                'Nome da variação 1,Valor da variação 1,Nome da variação 2,Valor da variação 2,' +
                'Preço,Preço promocional,' +
                'Peso,Altura,Largura,Comprimento,' +
                'Estoque,SKU,Código de barras,' +
                'Exibir na loja,Frete gratis,' +
                'Descrição,Tags,Título para SEO,Descrição para SEO\n'
        new File('test.csv').write(header + file)
    }

    static List productToCsv(Map product) {
        def id = StringUtils.stripAccents((product.title as String).toLowerCase()
                .replaceAll(/\s+/, '-'))

        GroovyCollections.combinations(product.sizes, product.colors).collect {
            def item = it as List
            [
                    id,
                    product.title,
                    '', // categorias TODO
                    'Tamanho',
                    item[0],
                    'Cor',
                    (item[1] as String).toLowerCase(),
                    '', // preço TODO
                    '', // preço promocional TODO
                    0, // peso TODO
                    0, // altura TODO
                    0, // largura TODO
                    0, // comprimento
                    '', // estoque
                    product.id,
                    '', // codigo de barra
                    'NÃO', // aparecer na loja
                    'NÃO',
                    formatDescription(product.description as String),
                    '', // tags
                    '', // titulo SEO
                    '', // descriçao SEO
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