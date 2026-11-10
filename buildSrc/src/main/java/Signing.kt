import java.io.File
import java.util.Properties

/**
 * Reads the release signing config from `keystore.properties` at the project root (the same layout
 * as the Compass project), falling back to environment variables used by CI.
 */
class Signing(private val rootDir: File) {

    data class Config(
        val storeFile: File,
        val storePassword: String,
        val keyAlias: String,
        val keyPassword: String,
    )

    fun readConfig(): Config? {
        val propFile = File(rootDir, "keystore.properties")
        if (!propFile.isFile) return null

        val prop = Properties()
        propFile.inputStream().use { prop.load(it) }

        fun value(propKey: String, legacyKey: String, envKey: String): String? =
            System.getenv(envKey) ?: prop.getProperty(propKey) ?: prop.getProperty(legacyKey)

        val storePath = prop.getProperty("storeFile") ?: return null
        val storePwd = value("storePassword", "storePwd", "WG_SIGNING_STORE_PWD") ?: return null
        val alias = value("keyAlias", "keyAlias", "WG_SIGNING_KEY_ALIAS") ?: return null
        val keyPwd = value("keyPassword", "keyPwd", "WG_SIGNING_KEY_PWD") ?: return null

        val storeFile = File(storePath).let { if (it.isAbsolute) it else File(rootDir, storePath) }
        if (!storeFile.isFile) return null

        return Config(storeFile, storePwd, alias, keyPwd)
    }
}
