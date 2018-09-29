package shop.queromania.labs.kero.parsers

import spock.lang.Specification
import spock.lang.Unroll

class InStockProductsCsvParserTest extends Specification {

    @Unroll
    def 'should parse lines'(List properties, Map result) {

        expect:
        InStockProductsCsvParser.parse(properties) == result

        where:
        properties | result
        [
                [sku: '000123', price: 10.0, color: 'black', size: 's'],
                [sku: '000456', price: 12.34, color: 'red', size: 'l']
        ]          | [
                '000123': [
                        sku     : '000123',
                        price   : [base: 10.0],
                        variants: [
                                [colors: ['black'], sizes: ['S']]
                        ]
                ],
                '000456': [
                        sku     : '000456',
                        price   : [base: 12.34],
                        variants: [
                                [colors: ['red'], sizes: ['L']]
                        ]
                ]
        ]
        [
                [sku: '000123', price: 10.0, color: 'black', size: 's'],
                [sku: '000456', price: 12.34, color: 'red', size: 'l'],
                [sku: '000123', price: 10.0, color: 'white', size: 'm']
        ]          | [
                '000123': [
                        sku     : '000123',
                        price   : [base: 10.0],
                        variants: [
                                [colors: ['black'], sizes: ['S']],
                                [colors: ['white'], sizes: ['M']]
                        ]
                ],
                '000456': [
                        sku     : '000456',
                        price   : [
                                base: 12.34,
                        ],
                        variants: [
                                [colors: ['red'], sizes: ['L']]
                        ]
                ]
        ]

    }

}
