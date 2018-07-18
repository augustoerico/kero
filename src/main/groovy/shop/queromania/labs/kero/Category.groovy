package shop.queromania.labs.kero

enum Category {

    SUTIAS('Sutiãs'),
    CALCINHAS('Calcinhas'),
    NOITE('Noite'),
    LEGGINGS_BODIES('Leggings e Bodies'),
    MASCULINO('Masculino'),
    PLUS_SIZE('Plus Size'),
    OFERTAS('Ofertas'),
    INFANTIL('Infantil')

    String name

    Category(String name) {
        this.name = name
    }

    static boolean isSutias(Map product) {
        product.tags.find { (it =~ /sutia/).size() }
    }

    static boolean isCalcinha(Map product) {
        product.tags.find { (it =~ /calcinha/).size() }
    }

    static boolean isNoite(Map product) {
        product.tags.find { (it =~ /noite/).size() }
    }

    static boolean isLeggingBodies(Map product) {
        product.tags.find {
            (it =~ /legging/).size() || (it =~ /body/).size()
        }
    }

    static boolean isMasculino(Map product) {
        boolean isZeus = product.tags.find { (it =~ 'zeus').size() }
        def normalizedFields = product.normalized
        boolean isMasculino =
                (normalizedFields.title =~ /masculino/).size() ||
                        (normalizedFields.description =~ /masculino/).size()
        isZeus || isMasculino
    }

    static boolean isPlusSize(Map product) {
        boolean isPlus = product.tags.find { (it =~ /plus/).size() }
        boolean isSizeGtMin = product.sizes.findAll { (it =~ /\d+/) }.any { it > '46' }
        isPlus || isSizeGtMin
    }

    static boolean isOfertas(Map product) {
        (product.price && product.discountPrice) && (product.discountPrice < product.price)
    }

    static boolean isInfantil(Map product) {
        product.tags.find { (it =~ /infantil/).size() }
    }

    static List<Category> getCategories(Map product) {
        def categories = []

        if (isSutias(product)) categories.add(SUTIAS)
        if (isCalcinha(product)) categories.add(CALCINHAS)
        if (isNoite(product)) categories.add(NOITE)
        if (isLeggingBodies(product)) categories.add(LEGGINGS_BODIES)
        if (isMasculino(product)) categories.add(MASCULINO)
        if (isPlusSize(product)) categories.add(PLUS_SIZE)
        if (isOfertas(product)) categories.add(OFERTAS)
        if (isInfantil(product)) categories.add(INFANTIL)

        categories
    }

    String toString() {
        this.name
    }
}
