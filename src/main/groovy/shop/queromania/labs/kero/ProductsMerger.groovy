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
        Map<String, Map> merged = [:]
        Map<String, Map> fetched = new JsonSlurper().parse(new File(paths.fetched)) as Map<String, Map>
        Map<String, Map> exported = new JsonSlurper().parse(new File(paths.exported)) as Map<String, Map>

        (fetched.keySet() + exported.keySet()).collect {
            String key = it.toString()
            if (key in exported.keySet() && key in fetched.keySet()) {
                def update = [
                        price        : fetched.get(key).price,
                        discountPrice: fetched.get(key).discountPrice,
                        display      : fetched.get(key).display
                ] as Map
                merged[key] = fetched.get(key) + exported.get(key) + update
            } else if (key in exported.keySet()) {
                def update = [display: false]
                merged[key] = (exported.get(key) as Map) + update
            } else {
                merged[key] = fetched.get(it)
            }
        }

        return merged
    }
}
