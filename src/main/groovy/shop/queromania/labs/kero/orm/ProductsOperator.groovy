package shop.queromania.labs.kero.orm

import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential
import com.mongodb.stitch.server.core.Stitch
import com.mongodb.stitch.server.services.mongodb.remote.RemoteMongoClient
import com.mongodb.stitch.server.services.mongodb.remote.RemoteMongoCollection
import org.yaml.snakeyaml.Yaml

class ProductsOperator {

    RemoteMongoCollection collection

    ProductsOperator() {
        def config = new Yaml()
                .load(new File('secrets/users.yaml').text)['mongo_stitch'] as Map<String, String>

        def appClient = Stitch.initializeAppClient(config['client_app_id'])

        appClient.auth.loginWithCredential(
                new UserPasswordCredential(config['username'], config['password'])
        )

        this.collection = appClient.getServiceClient(RemoteMongoClient.factory, 'mongodb-atlas')
                .getDatabase('products').getCollection('demillus')
    }

}
