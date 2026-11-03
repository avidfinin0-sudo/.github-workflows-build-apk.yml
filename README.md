# Xiaomi Island

Proyecto Android nativo en Kotlin para una isla flotante sencilla inspirada en HyperOS.

## Compatibilidad
- Android 8.0 (API 26) o superior.
- Pensado para probarse en teléfonos Android modestos; el rendimiento depende del sistema y del fabricante.
- `compileSdk 35`, `targetSdk 34`, Java 17, Android Gradle Plugin 8.7.3 y Gradle 8.9.

## Funciones incluidas
- Isla flotante compacta en la parte superior.
- Expandir y contraer al tocarla.
- Mostrar el título y el texto de notificaciones cuando se concede acceso a notificaciones.
- Mostrar estado básico de batería/carga dentro de la isla expandida.
- Notificación persistente mientras el servicio está activo.

No es una implementación oficial de Xiaomi HyperOS. No integra controles reales de reproducción multimedia, llamadas o temporizadores.

## Cómo probar en el teléfono
1. Instala el APK debug.
2. Abre Xiaomi Island.
3. Pulsa el botón de permiso para mostrarse sobre otras aplicaciones y actívalo.
4. Opcional: activa el acceso a notificaciones desde Ajustes si quieres que los avisos aparezcan en la isla.
5. Vuelve a la app y pulsa **Iniciar Xiaomi Island**.
6. Toca la isla para expandirla o contraerla.

Para detenerla, vuelve a la app y pulsa **Detener Xiaomi Island**.

## Compilar con GitHub Actions
1. Sube TODO el contenido de esta carpeta al nivel raíz de tu repositorio (no dentro de otra carpeta).
2. Comprueba que el archivo `.github/workflows/build-apk.yml` está en esa ruta exacta.
3. En GitHub abre **Actions** y ejecuta **Build Xiaomi Island APK** (o haz un commit en `main`).
4. Cuando el trabajo termine en verde, abre el resumen de la ejecución y descarga el artefacto **Xiaomi-Island-APK**.

El workflow no usa `android-actions/setup-android`; detecta `sdkmanager` en el Android SDK que trae el runner de GitHub.

## Nota
Este paquete contiene el código fuente y el workflow de compilación, no un APK ya compilado. La compilación real debe completarse en GitHub Actions o Android Studio.
