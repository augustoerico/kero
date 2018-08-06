package shop.queromania.labs.kero

import org.apache.commons.lang3.StringUtils

class Utils {

    static asNumber = {
        it instanceof String ?
                Float.parseFloat(it.replaceAll(/,/, '.')) :
                it
    }

    static normalize = {
        StringUtils.stripAccents(it as String).toLowerCase()
    }
}
