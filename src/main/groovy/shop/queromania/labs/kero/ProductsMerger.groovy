package shop.queromania.labs.kero

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ProductsMerger {

    def paths = [
            exported: 'outputs/products-exported.json',
            fetched : 'outputs/products-fetched.json'
    ]

    static main(args) {
        new File('outputs/products-merged.json').write(new JsonBuilder(
                new ProductsMerger().merge()
        ).toPrettyString(), 'UTF-8')
    }

    Map merge() {
        def merged = [:]
        def fetched = new JsonSlurper().parse(new File(paths.fetched)) as Map
        new JsonSlurper().parse(new File(paths.exported)).each { String k, v ->
            if (k in fetched.keySet()) {
                merged[k] = (fetched.get(k) + v + [display: true]) as Map
            } else {
                merged[k] = (v + [display: false]) as Map
            }
        }
        return merged
    }
}
