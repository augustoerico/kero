package shop.queromania.labs.kero

import spock.lang.Specification

class MainTests extends Specification {

    def 'should extract size list from description'(String description, List result) {
        expect:
        Main.getAvailableSizes(description) == result

        where:
        description         | result
        'tam.: 36 a 42'     | ['36', '38', '40', '42']
        'tamanhos: 42 a 48' | ['42', '44', '46', '48']
        'tam.: PE ME GR GG' | ['PE', 'ME', 'GR', 'GG']

    }

}
