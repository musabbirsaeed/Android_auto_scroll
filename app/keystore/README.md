# Release keystore folder

Place your release keystore in this folder (for example: `release.keystore`).

Do **not** commit the actual keystore file.

Create `keystore.properties` in the project root with:

```properties
storeFile=app/keystore/release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```
