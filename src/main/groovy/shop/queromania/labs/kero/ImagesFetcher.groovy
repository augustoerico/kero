package shop.queromania.labs.kero

class ImagesFetcher {

    static void fetch(String url, String name) {

        def ACCEPT = '*/*'
        def CONNECTION = 'keep-alive'
        def TOKEN = 'd7b540383018111a90b686758154559b01506215457' // TODO how to get this token?
        def USER_AGENT = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) ' +
                'Chrome/57.0.2987.110 Safari/537.36' // FIXME is this required?
        def COOKIE = "__cfduid=$TOKEN".toString()

        def format = 'jpg'
        def file = new File("images/$name.$format")
        file.withOutputStream { outputStream ->
            println url
            def connection = new URL(url).openConnection()
            connection.with {
                addRequestProperty('Accept', ACCEPT)
                addRequestProperty('Connection', CONNECTION)
                addRequestProperty('Cookie', COOKIE)
                addRequestProperty('User-Agent', USER_AGENT)
                outputStream << it.inputStream
            }
            outputStream.close()
        }
    }
}
