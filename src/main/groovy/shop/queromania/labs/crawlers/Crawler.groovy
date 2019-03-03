package shop.queromania.labs.crawlers

import org.jsoup.Jsoup

class Crawler {

    static Map<String, Map> crawl(List<String> skus, String baseUrl = "http://demillus.vestemuitomelhor.com.br/") {
        skus.collectEntries { sku ->
            def links = getLinksFromSearch("${baseUrl}?s=${sku}")
            def products = links.collect { getProduct(it) }
            [(sku): products.find { (it.sku as String).trim().padLeft(6, '0') == sku }]
        }
    }

    static List<String> getLinksFromSearch(String searchUrl) {
        def pageNode = Jsoup.connect(searchUrl).get()
        pageNode.select('div.list-item')?.collectMany {
            it.select('a[href*=pecas]').collect { it.attr('href') } as List<String>
        }
    }

    static Map getProduct(String productUrl) {
        def pageNode = Jsoup.connect(productUrl).get()
        def contentNode = pageNode.select('div.entry-content')?.first()
        def descriptionNode = contentNode.select('div.descriptions')?.first()


        [
                url        : productUrl,
                sku        : descriptionNode.select('span').first().text().find(~'\\w\\d{5}'),
                name       : contentNode?.select('h1.entry-title')?.first()?.text(),
                description: descriptionNode?.select('p')?.first()?.text(),
                exerpt     : contentNode?.select('p.excerpt')?.first()?.text(),
                imageLinks : contentNode?.select('div.images')?.first()
                        ?.select('a')?.collect { it.attr('href') },
                variants   : [
                        colors: contentNode?.select('div.cores')?.first()
                                ?.select('a.field-color')?.collect { it.text() },
                ],
                taxonomy   : pageNode?.select('ul#menu-principal')?.first()
                        ?.select('li.current-menu-parent > a')?.text()

        ]
    }

    static main(args) {
        def searchUrl = 'http://demillus.vestemuitomelhor.com.br/?s=63577'
        println(getLinksFromSearch(searchUrl))

        def productUrl = 'http://demillus.vestemuitomelhor.com.br/pecas/colecao-idylle-2/#main'
        println(getProduct(productUrl))

        def skus = ['063577', '053577']
        println(crawl(skus))
    }
}
