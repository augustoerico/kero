package shop.queromania.labs.kero

import spock.lang.Specification
import spock.lang.Unroll

class MainTests extends Specification {

    @Unroll
    def 'should extract size list from description'(String description, List result) {
        expect:
        ProductsFetcher.getAvailableSizes(description) == result

        where:
        description                                  | result
        'tam.: 36 a 42'                              | ['36', '38', '40', '42']
        'Tamanhos: 42 a 48'                          | ['42', '44', '46', '48']
        'Tam.: PE ME GR GG'                          | ['PE', 'ME', 'GR', 'GG']
        'TAMANHOS: p m g'                            | ['P', 'M', 'G']
        'tam.: UN (veste PE a GR)'                   | ['UN (PE - GR)']
        'TAMANHOS: U'                                | ['U']
    }

}
