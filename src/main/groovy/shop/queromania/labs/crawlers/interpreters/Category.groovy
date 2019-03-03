package shop.queromania.labs.crawlers.interpreters

enum Category {

    SUTIAS('Sutiãs'),
    CALCINHAS('Calcinhas'),
    NOITE('Noite'),
    LEGGINGS_BODIES('Leggings e Bodies'),
    MASCULINO('Masculino'),
    PLUS_SIZE('Plus Size'),
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
        product.tags.find { (it =~ /legging/).size() || (it =~ /body/).size() }
    }

    static boolean isMasculino(Map product) {
        boolean isZeus = product.tags.find { (it =~ 'zeus').size() }
        boolean isMasculino = (product.tags =~ /masculino/).size() || (product.tags =~ /masculino/).size()
        isZeus || isMasculino
    }

    static boolean isPlusSize(Map product) {
        boolean isPlus = product.tags.find { (it =~ /plus/).size() }
        boolean isSizeGtMin = product.variants.sizes.any {
            try {
                Integer.parseInt(it as String) > 48
            } catch (ignore) {
                false
            }
        }
        isPlus || isSizeGtMin
    }

    static boolean isInfantil(Map product) {
        boolean isInfantilTag = product.tags.find { (it =~ /infantil/).size() }
        boolean isSizeLtMax = product.variants.sizes.any {
            try {
                Integer.parseInt(it as String) <= 12
            } catch (ignore) {
                false
            }
        }
        isInfantilTag || isSizeLtMax
    }

    static List<Category> getCategories(Map product) {
        def categories = []

        if (isSutias(product)) categories.add(SUTIAS)
        if (isCalcinha(product)) categories.add(CALCINHAS)
        if (isNoite(product)) categories.add(NOITE)
        if (isLeggingBodies(product)) categories.add(LEGGINGS_BODIES)
        if (isMasculino(product)) categories.add(MASCULINO)
        if (isPlusSize(product)) categories.add(PLUS_SIZE)
        if (isInfantil(product)) categories.add(INFANTIL)

        categories
    }

    String toString() {
        this.name
    }
}
