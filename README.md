# FutbolStats - Grupo 67

## Índice

- [Integrantes](#integrantes)
- [Descripción del sistema](#descripción-del-sistema)
- [Requisitos](#requisitos)
- [Instrucciones de instalación](#instrucciones-de-instalación)
- [Instrucciones para la ejecución](#instrucciones-para-la-ejecución)
- [Datos de prueba](#datos-de-prueba)
- [¿Dónde se guardan los datos?](#dónde-se-guardan-los-datos)

## Integrantes

- Fausto Fernandez
- Ignacio Mendes
- Luka Kishima

## Descripción del sistema

1. Administra clubes, jugadores, entrenadores, árbitros, estadios y torneos (alta, consulta, modificación y baja).
2. Gestiona los contratos de los jugadores (alta, rescisión y transferencia).
3. Inscribe clubes en un torneo y genera el fixture todos contra todos.
4. Programa partidos y verifica la disponibilidad de estadios, clubes y árbitros.
5. Carga las nóminas y registra el partido en vivo: goles, remates, tarjetas, cambios y faltas.
6. Anula eventos sin borrarlos y aplica sanciones por tarjetas.
7. Avisa los goles, las expulsiones y el final del partido a los suscriptores por app móvil, correo y SMS.
8. Gestiona sponsors y patrocinios de clubes y torneos.
9. Emite reportes (tabla de posiciones, goleadores y valla menos vencida) y calcula la probabilidad de clasificación con dos métodos: promedio de puntos y simulación con xG.
10. Guarda la información en un archivo para conservarla entre ejecuciones.

## Requisitos

- IntelliJ IDEA o Visual Studio Code
- JUnit 5 para las pruebas unitarias
- Java 17

## Instrucciones de instalación

1. Abrir la carpeta del proyecto (`Grupo67`).
2. Verificar en **File → Project Structure** que el SDK sea Java 17, que `src` esté marcada como *Sources* y `test` como *Test Sources*.
3. Ejecutar la clase `Main` (clic derecho → **Run 'Main.main()'**).

## Instrucciones para la ejecución

Todo es por consola: escribís el número de la opción y le das Enter.

- **Opción 1:** Datos → cargar, ver, modificar y dar de baja clubes, jugadores, entrenadores, árbitros, estadios y torneos.
- **Opción 2:** Contratos de jugadores.
- **Opción 3:** Torneos → inscripción, fixture y estado.
- **Opción 4:** Partidos → programarlos, cargar nóminas y jugarlos en vivo.
- **Opción 5:** Suscripciones y notificaciones.
- **Opción 6:** Sponsors y patrocinios.
- **Opción 7:** Reportes y proyecciones.
- **Opción 8:** Cargar los datos de prueba.
- **Opción 0:** Salir (y guardar).

Las fechas van como `aaaa-mm-dd` y las fechas con hora como `aaaa-mm-dd hh:mm` (por ejemplo, `2026-10-20 21:15`).

## Datos de prueba

Si no querés cargar todo a mano, está la opción 8, que ya tiene un torneo armado con información (clubes, árbitros, estadios) para probar:

- **Torneo:** «Liga Profesional 2026», del 2026-03-01 al 2026-12-15, nacional, 2 cupos para clasificar y corte de 30 puntos.
- **Clubes:** River Plate, Boca Juniors, Racing Club e Independiente, con 20 jugadores y un entrenador cada uno.
- **Fixture:** 3 fechas y 6 partidos, todavía sin programar.
- **Estadios:** Monumental, La Bombonera y Cilindro de Avellaneda.
- **Árbitros:** Arbitro1 a Arbitro9, categoría nacional.
- **Sponsor:** Deportes SA (marca Sportline).
- **Suscriptor:** Marcos Prensa: recibe por app, correo y SMS, y sigue a River y a Boca.

**Para jugar un partido rápido:** opción `4` → torneo `1` → partido `1` → programalo (`2026-05-10 18:00`, estadio `1`, árbitros `1`, `1`, `1`) → cargá las dos nóminas (automática: `s`) → inicialo → meté goles, tarjetas o lo que quieras → finalizalo. Después andá a la opción `7` y mirá cómo quedó la tabla, los goleadores y las proyecciones.

Si querés probar todo a fondo, en `docs` está el plan de pruebas funcionales con cada caso paso a paso.

## ¿Dónde se guardan los datos?

En el archivo **`futbolstats.dat`**, en la carpeta desde donde corrés el programa (con IntelliJ, la del proyecto).

- **Al abrir:** si el archivo existe, carga todo lo que tenías. Si no existe, arranca de cero.
- **Al salir** (opción `0`): guarda todo.
- **Si el archivo se rompió:** te avisa, arranca vacío y el programa sigue andando normal.

Si querés empezar de cero, borrá `futbolstats.dat` y listo. Ese archivo no se sube al repo (está en el `.gitignore`).

