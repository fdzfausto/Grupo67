# Resumen de cambios del diagrama de clases (Etapa 1 → Etapa 2)

**Grupo 67 – FutbolStats.** Archivo del diagrama: `Diagrama_Clases_Etapa2.drawio`. El diagrama final de Etapa 1 se conserva sin cambios.

Durante la codificación priorizamos que el sistema funcione y cumpla las reglas de negocio. Por eso el diagrama se actualizó al final para que coincida con el código real. No se cambiaron reglas de negocio para adaptarlas al diagrama.

## 1. Clases agregadas
- **`Fixture`**: guarda las fechas y los partidos del torneo y los genera con el método del círculo (todos contra todos, una rueda).
- **`TipoTorneo`** (enumeración): distingue torneos nacionales de internacionales.
- **Reportes:** `FilaTabla` y `Goleador` (implementan `Comparable`) y `ComparadorTabla` (implementa `Comparator`) resuelven el ordenamiento de la tabla y de los goleadores.
- **Excepciones:** `ReglaNegocioException` (reglas de negocio) y `PersistenciaException` (guardado y carga de archivos).
- **Persistencia:** `RepositorioArchivo` guarda y carga los datos en un archivo.
- **Interfaz de consola:** `Main`, `Menu`, `Consola` y `Datos`. El menú pide los datos y delega las reglas en los controladores y el modelo.

No se eliminó ninguna clase de Etapa 1. Los tres controladores (`ControladorTorneo`, `ControladorPartido` y `ControladorReportes`) ya estaban en el diagrama y ahora existen en el código.

## 2. Relaciones que cambiaron
- `Torneo` compone un `Fixture` y este compone los `Partido`. Antes `Torneo` componía los partidos directamente.
- `Torneo` compone sus `Sancion`. La sanción se asocia con `Jugador`, `Club` y el `Partido` de origen.
- Nueva asociación `Arbitro`–`Club` (clubes de los que es hincha), que se usa para validar la designación de árbitros.
- `Menu` se asocia con `Datos`, `Consola`, `RepositorioArchivo` y los tres controladores.
- Se dibujaron las implementaciones de `Comparable` y `Comparator`, y las dependencias de los servicios y controladores.

## 3. Atributos, métodos y responsabilidades
- **`Torneo`** ahora valida la programación de partidos (estadio, clubes, árbitros y período), carga nóminas rechazando jugadores suspendidos y registra sanciones. Antes solo inscribía clubes y generaba el fixture.
- **`Partido`** valida cada evento (nómina, cambios, expulsiones y anulaciones) y calcula goles y xG.
- **`Evento`** y sus subclases responden por polimorfismo (`golesPara`, `expulsa`, `sustituye`, `actualizarEstadisticas`, etc.), de modo que `Partido` no necesita preguntar de qué clase es cada evento.
- **`Inscripcion`** valida el plantel (18 a 30 jugadores) al crearse; ya no tiene `agregarJugador`, `quitarJugador` ni `validarPlantel`.
- **`Sancion`** representa una sola fecha de suspensión y se marca como cumplida; se quitó `fechasSuspension`.
- **`Jugador`** suma goles, tarjetas y partidos jugados, y maneja contratos (alta, transferencia y rescisión).
- **`ServicioReportes.obtenerGoleadores`** devuelve `List<Goleador>` en lugar de `List<Jugador>`.
- Las enumeraciones usan la convención de Java (`EN_JUEGO`, `AREA_CHICA`).

## 4. Decisiones de diseño
- **Controlador:** la interfaz de consola llama a los controladores y estos al modelo, así la interfaz no conoce las reglas.
- **Experto y Creador:** `Torneo` conoce todos sus partidos, por eso valida y crea sanciones, inscripciones y el fixture.
- **Polimorfismo y Variaciones protegidas:** los eventos, los canales de notificación y los métodos de proyección se pueden ampliar sin tocar las clases que los usan.
- **Fabricación pura:** la persistencia y los servicios de reportes y notificaciones están separados del modelo.
- **Principio abierto/cerrado:** los canales de notificación y las proyecciones se extienden con clases nuevas.

Cualquier diferencia no listada es un detalle de implementación (getters, constantes, métodos auxiliares privados), que no se representa para mantener el diagrama legible.
