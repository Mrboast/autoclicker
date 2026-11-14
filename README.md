# Air AutoClicker (Fabric 1.21.1)

Clic izquierdo automático "al aire". Sin aim assist ni movimiento de cámara.

- **R**: iniciar / detener (configurable en Opciones > Controles > Air AutoClicker)
- **Shift derecho**: abrir el menú con el slider de intervalo (0.8 s a 3 min)
- Config guardada en `config/airclicker.json`

## Compilar
1. Instala JDK 21.
2. Si no tienes el wrapper de Gradle: `gradle wrapper --gradle-version 8.8` (o copia el wrapper de la plantilla de https://fabricmc.net/develop/template).
3. `./gradlew build` → el .jar queda en `build/libs/`.
4. Ponlo en `.minecraft/mods` junto a Fabric API.
Hecho por mrboast
