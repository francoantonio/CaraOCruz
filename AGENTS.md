# AGENTS.md

Guía para agentes de IA que trabajen en este repositorio. Para la descripción general, ver `README.md`.

## Qué es

App Android "Cara o Cruz y Dados" (`dev.antoniofranco.caraocruz`): moneda, dado de 6 caras, historial de 10 tiradas y estadísticas. Kotlin + Jetpack Compose + Material 3. Una sola Activity.

## Principios que no se rompen

- **Sin permisos, sin internet, sin anuncios, sin analytics, sin SDKs de terceros.** Es una promesa de la ficha y de la política de privacidad.
- **No se guarda nada en disco.** El historial vive solo en memoria (`rememberSaveable`). Si algún cambio persiste datos, agrega un permiso o una conexión, hay que actualizar en el mismo cambio `store/politica-privacidad.md` y `store/ficha.md`, y avisar que cambia el formulario de Seguridad de los datos en Play Console.
- No agregar dependencias sin necesidad clara; la app tiene que seguir siendo chica.

## Comandos

```bash
./gradlew assembleDebug     # compilar debug
./gradlew bundleRelease     # AAB de release (firmado si existe ~/keys/caraocruz.properties)
./gradlew lint              # lint de Android
```

Verificá que `assembleDebug` compile antes de dar un cambio por terminado. Todavía no hay tests.

## Convenciones de código

- Toda la UI está en `app/src/main/java/dev/antoniofranco/caraocruz/MainActivity.kt`. Si crece mucho, dividir por pantalla (`CoinScreen`, `DiceScreen`) en archivos separados del mismo paquete.
- Composables `private` salvo que haga falta otra cosa.
- El estado que debe sobrevivir a la rotación usa `rememberSaveable`; para listas usar los savers existentes `listSaver()` / `intListSaver()`. El estado por pestaña se conserva con `rememberSaveableStateHolder` en `App()`.
- Colores del tema en `AppTheme()`; respetar modo claro y oscuro.
- Textos visibles siempre en `res/values/strings.xml`, nunca hardcodeados. Español rioplatense con voseo ("Tocá", "Tirá").
- Versiones de dependencias y plugins solo en `gradle/libs.versions.toml`.
- Comentarios en español.

## Releases

1. Subir `versionCode` (+1) y `versionName` en `app/build.gradle.kts`.
2. Actualizar "Notas de la versión" en `store/ficha.md`.
3. `./gradlew bundleRelease`.

## Assets de la tienda

- `store/raw/` tiene las capturas originales (claro y oscuro).
- `store/html/*.html` son plantillas que componen esas capturas; se renderizan a `store/out/*.png` con los tamaños de Play (ícono 512×512, gráfico 1024×500, capturas 1080×1920).
- Si cambia la UI, regenerar capturas y PNGs.

## Seguridad

- Nunca commitear keystores (`*.jks`, `*.keystore`), `local.properties` ni nada de `~/keys/`.
- No modificar el bloque `signingConfigs` de `app/build.gradle.kts` sin que te lo pidan.
