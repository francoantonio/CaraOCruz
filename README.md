# Cara o Cruz y Dados

Tirá una moneda o un dado en un toque. Simple, rápido y sin anuncios.

App Android hecha con Kotlin y Jetpack Compose. Funciona sin conexión, no pide permisos y no recopila datos.

## Funciones

- Moneda con animación de giro: Cara o Cruz.
- Dado de 6 caras.
- Historial de las últimas 10 tiradas.
- Estadísticas: cantidad de caras y cruces, y promedio del dado.
- Modo claro u oscuro según el sistema.
- Vibración al tirar.

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.4.21 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3 |
| Build | AGP 9.5.1, Gradle 9.9.0, JDK 17 |
| SDK | minSdk 26 · targetSdk/compileSdk 37 |

Las versiones de las dependencias están en `gradle/libs.versions.toml`.

## Estructura

```
app/src/main/java/dev/antoniofranco/caraocruz/MainActivity.kt   toda la UI y la lógica
app/src/main/res/values/strings.xml                             textos de la app
store/ficha.md                     ficha de Play Store
store/politica-privacidad.md       política de privacidad
store/raw/                         capturas originales de la app
store/html/                        plantillas de las imágenes de la tienda
store/out/                         imágenes finales (ícono, gráfico, capturas)
```

## Compilar

```bash
./gradlew assembleDebug     # APK de debug → app/build/outputs/apk/debug/
./gradlew bundleRelease     # AAB para Play → app/build/outputs/bundle/release/app-release.aab
```

### Firma de release

El build de release se firma con la clave de upload si existe `~/keys/caraocruz.properties`:

```properties
storeFile=/ruta/a/caraocruz-upload.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Sin ese archivo, `bundleRelease` genera un AAB sin firmar. La clave y sus contraseñas nunca van al repositorio.

## Privacidad

- Versión publicada: https://francoantonio.github.io/CaraOCruz/privacidad.html
- Texto en el repositorio: [store/politica-privacidad.md](store/politica-privacidad.md)

## Roadmap

**Publicación 1.0**
- [x] Publicar la política de privacidad en una URL pública (GitHub Pages).
- [ ] Crear la app en Play Console y cargar ficha, imágenes y AAB.
- [ ] Completar seguridad de datos, clasificación de contenido, público objetivo y anuncios.
- [ ] Prueba cerrada (12 testers durante 14 días) y pase a producción.

**Ideas 1.x**
- Guardar el historial entre sesiones (implica actualizar la política de privacidad).
- Sacudir el teléfono para tirar.
- Dados de otras caras (d4, d8, d12, d20) y varios dados a la vez.
- Traducción al inglés.
