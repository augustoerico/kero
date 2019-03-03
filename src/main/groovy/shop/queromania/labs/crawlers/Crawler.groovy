package shop.queromania.labs.crawlers

import org.jsoup.Jsoup

class Crawler {

    static List<String> getLinksFromSearch(String searchUrl) {
        def pageNode = Jsoup.connect(searchUrl).get()
        pageNode.select('div.list-item')?.collectMany {
            it.select('a[href*=pecas]').collect { it.attr('href') } as List<String>
        }
    }

    static main(args) {
        def url = 'http://demillus.vestemuitomelhor.com.br/?s=63577'
        println(getLinksFromSearch(url))
    }
}
