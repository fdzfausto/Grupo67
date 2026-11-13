# Plan de pruebas funcionales – FutbolStats

## ¿De qué se trata?

Acá están las pruebas que hicimos usando el programa como lo usaría cualquiera: abrimos el menú, cargamos datos y nos fijamos que haga lo que tiene que hacer. Para cada caso anotamos qué ingresamos, qué esperábamos que pase y qué pasó de verdad.

Esto se suma a los tests unitarios de la carpeta `test/`, que prueban las reglas de cada clase por separado.

Corrimos todos los casos el 12/11/2026 sobre la versión final del programa y **dieron todos bien**.

## ¿Qué casos de uso cubrimos?

| Caso de uso | Qué hace | Opción del menú |
|---|---|---|
| CU01 | Administrar datos (alta, consulta, modificación y baja) | 1 |
| CU02 | Gestionar contratos | 2 |
| CU03 | Inscribir club en torneo | 3 |
| CU04 | Generar fixture | 3 |
| CU05 | Programar partido | 4 |
| CU06 | Cargar nómina | 4 |
| CU07 | Registrar partido en vivo | 4 |
| CU08 | Anular evento | 4 |
| CU09 | Notificar hecho del partido | 4 (sale solo) |
| CU10 | Administrar suscripción | 5 |
| CU11 | Gestionar patrocinios | 6 |
| CU12 | Emitir reportes | 7 |
| CU13 | Calcular proyecciones | 7 |
| CU14 | Verificar disponibilidad | 4 (al programar) |
| Persistencia | Guardar y recuperar los datos entre ejecuciones | Automático (al abrir y al salir) |

Los avisos por app, correo y SMS se simulan mostrándolos en la consola.

## Antes de arrancar

Cómo correr el programa y qué carga la opción **8** (datos de prueba) está en el **README**. Acá solo va lo que hace falta para seguir los pasos:

- **Números de la lista:** clubes 1) River Plate, 2) Boca Juniors, 3) Racing Club, 4) Independiente. Estadios 1) Monumental, 2) La Bombonera, 3) Cilindro de Avellaneda. Partidos 1) River – Independiente, 2) Racing – Boca, 3) Racing – River, 4) Independiente – Boca, 5) River – Boca, 6) Independiente – Racing.
- **Cómo leer los pasos:** algo como `4 → 1 → 1` son las opciones que vas eligiendo, en orden. Lo que está entre comillas es lo que hay que escribir.
- **Ojo con el orden:** los casos PF-01 a PF-22 van seguidos en la misma ejecución, porque cada uno usa lo que dejó el anterior. Los de persistencia (PF-23 a PF-25) necesitan cerrar y volver a abrir el programa, y los del CU01 (PF-26 y PF-27) se hacen en una ejecución nueva.
- **Para tener en cuenta:** los eventos de un partido se listan en el orden en que se cargaron, no por minuto, y la probabilidad por xG (PF-22) puede cambiar un poco entre ejecuciones porque es una simulación.

## Casos de prueba

### Administración de datos, torneos y fixture (CU01, CU03, CU04)

#### PF-01 – Cargar datos de demostración

| Campo | Contenido |
|---|---|
| Caso de uso | CU01 |
| Objetivo | Que se cargue el torneo de ejemplo con sus clubes, jugadores y fixture |
| Precondiciones | Programa recién iniciado, sin datos |
| Datos y pasos | 1. Ingresar `8` en el menú principal. 2. Ingresar `1`, luego `1` y elegir «Torneos» para ver el listado |
| Resultado esperado | Mensaje de carga de datos de demostración. El listado de torneos muestra «Liga Profesional 2026 (PLANIFICADO)» |
| Resultado obtenido | Se muestra «Datos de demostración cargados: torneo con 4 clubes y fixture generado.» El listado de torneos muestra «Liga Profesional 2026 (PLANIFICADO)». |
| Estado | **OK** |

#### PF-02 – Alta de torneo con datos válidos

| Campo | Contenido |
|---|---|
| Caso de uso | CU01 |
| Objetivo | Que se pueda crear un torneo nuevo |
| Precondiciones | PF-01 ejecutado |
| Datos y pasos | 1. `1 → 7`. 2. Nombre: «Copa Nacional 2026». 3. Inicio: `2026-03-01`. 4. Fin: `2026-12-15`. 5. Tipo: `1` (NACIONAL). 6. Cupos: `2`. 7. Puntaje de corte: `30` |
| Resultado esperado | Mensaje «Torneo creado: Copa Nacional 2026 (PLANIFICADO)». El torneo aparece como segundo en el listado |
| Resultado obtenido | Se muestra «Torneo creado: Copa Nacional 2026 (PLANIFICADO)». El listado muestra los dos torneos y «Copa Nacional 2026» figura segundo. |
| Estado | **OK** |

#### PF-03 – Consultar el fixture

| Campo | Contenido |
|---|---|
| Caso de uso | CU04 |
| Objetivo | Que en el fixture todos jueguen contra todos una vez |
| Precondiciones | PF-01 ejecutado |
| Datos y pasos | `3 → 3` y elegir el torneo `1` |
| Resultado esperado | Se muestran 3 fechas con 2 partidos cada una (6 partidos en total), todos en estado SIN_PROGRAMAR. Ningún club juega dos veces en la misma fecha |
| Resultado obtenido | Se muestran 3 fechas con 2 partidos cada una (6 en total), todos SIN_PROGRAMAR. Ningún club juega dos veces en la misma fecha. |
| Estado | **OK** |

#### PF-04 – Inscribir un club ya inscripto (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU03 |
| Objetivo | Que un club no se pueda anotar dos veces en el mismo torneo |
| Precondiciones | PF-01 ejecutado |
| Datos y pasos | `3 → 1`. Torneo: `1`. Club: `1` (River Plate) |
| Resultado esperado | El sistema informa que River Plate ya está inscripto en «Liga Profesional 2026». No se modifican las inscripciones |
| Resultado obtenido | Se muestra «No se pudo completar la operación: River Plate ya está inscripto en Liga Profesional 2026». Las inscripciones no cambian. |
| Estado | **OK** |

#### PF-05 – Iniciar un torneo con menos de 4 clubes (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU03 |
| Objetivo | Que un torneo no arranque con menos de 4 clubes |
| Precondiciones | PF-02 ejecutado (torneo «Copa Nacional 2026» sin clubes) |
| Datos y pasos | `3 → 4`. Torneo: `2` |
| Resultado esperado | El sistema informa que el torneo necesita al menos 4 clubes inscriptos para comenzar (tiene 0). El estado sigue siendo PLANIFICADO |
| Resultado obtenido | Se muestra «No se pudo completar la operación: El torneo necesita al menos 4 clubes inscriptos para comenzar (tiene 0)». El torneo sigue PLANIFICADO. |
| Estado | **OK** |

### Contratos de jugadores (CU02)

#### PF-06 – Transferir un jugador a otro club

| Campo | Contenido |
|---|---|
| Caso de uso | CU02 |
| Objetivo | Que al transferir se cierre el contrato viejo, se abra uno nuevo y quede el historial |
| Precondiciones | PF-01 ejecutado. Jugador20 de River Plate con contrato vigente |
| Datos y pasos | 1. `2 → 2`. 2. ¿Pertenece a un club?: `s`. 3. Club: `1` (River Plate). 4. Jugador: `20`. 5. Club de destino: `2` (Boca Juniors). 6. Fecha: `2026-07-01`. 7. Volver al menú y entrar a `2 → 4` para ver el historial del jugador (club `2`, jugador `1`: el transferido figura primero en la lista) |
| Resultado esperado | Mensaje de transferencia de Jugador20 a Boca Juniors. El historial muestra dos contratos: el de River Plate, finalizado el 2026-07-01, y el de Boca Juniors, vigente desde esa fecha |
| Resultado obtenido | Se muestra «Jugador20 ... fue transferido a Boca Juniors». El historial muestra «River Plate (2026-01-01 - 2026-07-01)» y «Boca Juniors (2026-07-01 - Vigente)». |
| Estado | **OK** |

### Programación de partidos y disponibilidad (CU05, CU14)

#### PF-07 – Programar un partido con datos válidos

| Campo | Contenido |
|---|---|
| Caso de uso | CU05 |
| Objetivo | Que se pueda programar un partido con fecha, estadio y terna |
| Precondiciones | PF-01 ejecutado. Partido 1 (River Plate – Independiente) SIN_PROGRAMAR |
| Datos y pasos | 1. `4`, torneo `1`, partido `1`. 2. Opción `1` (programar). 3. Fecha y hora: `2026-05-10 18:00`. 4. Estadio: `1` (Monumental). 5. Árbitro principal: `1`. 6. Primer asistente: `1`. 7. Segundo asistente: `1` (la terna queda Arbitro1, Arbitro2, Arbitro3) |
| Resultado esperado | Mensaje de partido programado el 2026-05-10 a las 18:00 en el Monumental. El partido pasa a estado PROGRAMADO |
| Resultado obtenido | Se muestra «Partido programado el 2026-05-10T18:00 en Monumental (84000 personas).» El partido pasa a PROGRAMADO. |
| Estado | **OK** |

#### PF-08 – Programar un partido fuera del período del torneo (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU05, CU14 |
| Objetivo | Que no se pueda programar un partido fuera de las fechas del torneo |
| Precondiciones | PF-01 ejecutado. Partido 5 (River Plate – Boca Juniors) SIN_PROGRAMAR |
| Datos y pasos | `4`, torneo `1`, partido `5`, opción `1`. Fecha y hora: `2027-01-10 18:00`. Estadio: `1`. Terna: `1`, `1`, `1` |
| Resultado esperado | El sistema informa que la fecha 2027-01-10 está fuera del período del torneo (2026-03-01 a 2026-12-15). El partido sigue SIN_PROGRAMAR |
| Resultado obtenido | Se muestra «La fecha 2027-01-10 está fuera del período del torneo (2026-03-01 a 2026-12-15)». El partido sigue SIN_PROGRAMAR. |
| Estado | **OK** |

#### PF-09 – Estadio ocupado con menos de 3 horas de diferencia (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU14 |
| Objetivo | Que un estadio no tenga dos partidos con menos de 3 horas de diferencia |
| Precondiciones | PF-07 ejecutado (Monumental ocupado el 2026-05-10 a las 18:00). Partido 2 (Racing Club – Boca Juniors) SIN_PROGRAMAR |
| Datos y pasos | `4`, torneo `1`, partido `2`, opción `1`. Fecha y hora: `2026-05-10 20:00`. Estadio: `1` (Monumental). Terna: `4`, `4`, `4` (Arbitro4, Arbitro5, Arbitro6) |
| Resultado esperado | El sistema informa que el estadio ya tiene un partido con menos de 3 horas de diferencia. El partido 2 sigue SIN_PROGRAMAR |
| Resultado obtenido | Se muestra «Monumental (84000 personas) ya tiene el partido River Plate 0 - 0 Independiente (PROGRAMADO) con menos de 3 horas de diferencia». El partido 2 sigue SIN_PROGRAMAR. |
| Estado | **OK** |

#### PF-10 – Club con dos partidos en menos de 48 horas (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU14 |
| Objetivo | Que un club no juegue dos partidos con menos de 48 horas de diferencia |
| Precondiciones | PF-07 ejecutado (River Plate juega el 2026-05-10 a las 18:00). Partido 3 (Racing Club – River Plate) SIN_PROGRAMAR |
| Datos y pasos | `4`, torneo `1`, partido `3`, opción `1`. Fecha y hora: `2026-05-11 18:00`. Estadio: `2` (La Bombonera). Terna: `7`, `7`, `7` (Arbitro7, Arbitro8, Arbitro9) |
| Resultado esperado | El sistema informa que un club del partido ya juega con menos de 48 horas de diferencia. El partido 3 sigue SIN_PROGRAMAR |
| Resultado obtenido | Se muestra «Un club del partido ya juega River Plate 0 - 0 Independiente (PROGRAMADO) con menos de 48 horas de diferencia». El partido 3 sigue SIN_PROGRAMAR. |
| Estado | **OK** |

### Nóminas y partido en vivo (CU06, CU07, CU08, CU09)

#### PF-11 – Cargar las nóminas e iniciar el partido

| Campo | Contenido |
|---|---|
| Caso de uso | CU06, CU07 |
| Objetivo | Que se carguen las nóminas y que el partido no arranque sin las dos |
| Precondiciones | PF-07 ejecutado (partido 1 PROGRAMADO) |
| Datos y pasos | 1. `4`, torneo `1`, partido `1`. 2. Opción `2`, club `1` (River Plate), nómina automática `s`. 3. Opción `3` (iniciar). 4. Opción `2`, club `2` (Independiente), nómina automática `s`. 5. Opción `3` (iniciar) |
| Resultado esperado | Paso 2: nómina de River Plate cargada (11 titulares, 5 suplentes). Paso 3: el sistema informa que deben estar cargadas las nóminas de ambos clubes y el partido sigue PROGRAMADO. Paso 4: nómina de Independiente cargada. Paso 5: mensaje «El partido está EN_JUEGO» |
| Resultado obtenido | Paso 2: «Nómina cargada: Nómina de River Plate (11 titulares, 5 suplentes)». Paso 3: «Para iniciar el partido deben estar cargadas las nóminas de ambos clubes». Paso 4: nómina de Independiente cargada. Paso 5: «El partido está EN_JUEGO.» |
| Estado | **OK** |

#### PF-12 – Registrar un gol y notificar a los suscriptores

| Campo | Contenido |
|---|---|
| Caso de uso | CU07, CU09 |
| Objetivo | Que el gol sume al marcador y les llegue el aviso a los suscriptores |
| Precondiciones | PF-11 ejecutado (partido 1 EN_JUEGO) |
| Datos y pasos | En el menú del partido 1: opción `4`. Tipo de evento: `1` (Gol). Club: `1` (River Plate). Jugador: `10`. Minuto: `15`. Tipo de gol: `1` (A_FAVOR) |
| Resultado esperado | Mensaje de evento registrado (minuto 15, gol de Jugador10). El marcador pasa a River Plate 1 – 0 Independiente. Se muestran tres avisos para Marcos Prensa (app móvil, correo y SMS) con el partido, el minuto y el hecho |
| Resultado obtenido | Se muestra «Evento registrado: min 15 - Gol de Jugador10 de River». Marcador River Plate 1 - 0 Independiente. Llegan los tres avisos a Marcos Prensa ([App móvil], [Correo] y [SMS]) con el partido, el minuto 15 y el gol. |
| Estado | **OK** |

#### PF-13 – Registrar remates y calcular el xG del partido

| Campo | Contenido |
|---|---|
| Caso de uso | CU07, CU13 |
| Objetivo | Que el xG del club sea la suma de sus remates según la zona |
| Precondiciones | PF-12 ejecutado |
| Datos y pasos | 1. Opción `4`, tipo `2` (Remate), club `1`, jugador `9`, minuto `25`, zona `1` (AREA_CHICA). 2. Opción `4`, tipo `2`, club `1`, jugador `8`, minuto `35`, zona `3` (FUERA_DEL_AREA). 3. Volver al menú principal y entrar a `7`, torneo `1`, opción `4`, partido `1` |
| Resultado esperado | Los dos remates se registran. El xG de River Plate es 0,44 (0,40 + 0,04) y el de Independiente es 0,00 |
| Resultado obtenido | Se registran los dos remates (AREA_CHICA y FUERA_DEL_AREA). Se muestra «xG de River Plate: 0.44» y «xG de Independiente: 0.00». |
| Estado | **OK** |

#### PF-14 – Doble amarilla: expulsión automática y restricciones del expulsado

| Campo | Contenido |
|---|---|
| Caso de uso | CU07, CU09 |
| Objetivo | Que dos amarillas sean roja, que se avise y que el expulsado no pueda hacer nada más |
| Precondiciones | PF-12 ejecutado (partido 1 EN_JUEGO) |
| Datos y pasos | 1. Opción `4`, tipo `3` (Tarjeta amarilla), club `2` (Independiente), jugador `3`, minuto `20`. 2. Repetir con el mismo jugador y minuto `60`. 3. Opción `4`, tipo `2` (Remate), club `2`, jugador `3`, minuto `70`, zona `1` |
| Resultado esperado | Paso 1: amarilla registrada sin notificación. Paso 2: se registra la amarilla y el sistema agrega una tarjeta roja para el mismo jugador, con notificación de tarjeta roja por los tres canales. Paso 3: el sistema rechaza el remate porque el jugador está expulsado |
| Resultado obtenido | Paso 1: amarilla registrada sin aviso. Paso 2: se registra la amarilla y el sistema agrega «Tarjeta roja para Jugador3 de Independiente (min 60)», con aviso de tarjeta roja por los tres canales. Paso 3: «Jugador3 de Independiente ... está expulsado y no puede participar de más eventos». |
| Estado | **OK** |

#### PF-15 – Anular un evento

| Campo | Contenido |
|---|---|
| Caso de uso | CU08 |
| Objetivo | Que un evento anulado no se borre, quede marcado y deje de contar en el marcador |
| Precondiciones | PF-12 ejecutado (marcador 1 – 0) |
| Datos y pasos | 1. Opción `4`, tipo `1` (Gol), club `2` (Independiente), jugador `10`, minuto `30`, tipo de gol `1`. El marcador pasa a 1 – 1. 2. Opción `5` y elegir ese gol. Motivo: «Error de carga». 3. Opción `6` (ver eventos) |
| Resultado esperado | Paso 2: mensaje de evento anulado y el marcador vuelve a River Plate 1 – 0 Independiente. Paso 3: el gol de Independiente figura marcado como [ANULADO] y la anulación aparece como evento del partido |
| Resultado obtenido | Paso 1: marcador 1 - 1 (con aviso del gol). Paso 2: «Evento anulado (queda registrado junto con su anulación). River Plate 1 - 0 Independiente». Paso 3: el gol figura «[ANULADO]» y aparece el evento «Anulado: ... - Motivo: Error de carga». |
| Estado | **OK** |

#### PF-16 – Finalizar el partido

| Campo | Contenido |
|---|---|
| Caso de uso | CU07, CU09 |
| Objetivo | Que al finalizar se cierre el partido, se registren las sanciones y se avise el final |
| Precondiciones | PF-14 y PF-15 ejecutados |
| Datos y pasos | En el menú del partido 1: opción `7` (finalizar) |
| Resultado esperado | Mensaje «Partido finalizado: River Plate 1 – 0 Independiente (FINALIZADO)». Se informa la sanción pendiente de Jugador3 de Independiente por tarjeta roja. Se envía la notificación de final del partido por los tres canales |
| Resultado obtenido | Se muestra «Partido finalizado: River Plate 1 - 0 Independiente (FINALIZADO)», la sanción «Jugador3 de Independiente ... Tarjeta roja - pendiente» y el aviso del final (min 90) por los tres canales. |
| Estado | **OK** |

#### PF-17 – Jugador suspendido no puede incluirse en la nómina

| Campo | Contenido |
|---|---|
| Caso de uso | CU06 |
| Objetivo | Que un jugador suspendido no pueda entrar en la nómina del partido siguiente |
| Precondiciones | PF-16 ejecutado (Jugador3 de Independiente suspendido). Partido 4 (Independiente – Boca Juniors) SIN_PROGRAMAR |
| Datos y pasos | 1. `4`, torneo `1`, partido `4`, opción `1`. Fecha y hora: `2026-05-14 18:00`. Estadio: `3`. Terna: `1`, `1`, `1`. 2. Opción `2`, club `1` (Independiente), nómina automática `s` |
| Resultado esperado | Paso 1: partido programado. Paso 2: el sistema informa que Jugador3 está suspendido y no puede incluirse en la nómina. La nómina no se carga |
| Resultado obtenido | Paso 1: «Partido programado el 2026-05-14T18:00 en Cilindro de Avellaneda». Paso 2: «Jugador3 de Independiente ... está suspendido y no puede incluirse en la nómina». La nómina no se carga. |
| Estado | **OK** |

### Suscripciones y patrocinios (CU10, CU11)

#### PF-18 – Alta de suscriptor, elección de canales y club seguido

| Campo | Contenido |
|---|---|
| Caso de uso | CU10 |
| Objetivo | Que se pueda crear un suscriptor y elegir sus canales y clubes |
| Precondiciones | PF-01 ejecutado |
| Datos y pasos | 1. `5 → 1`. Nombre: «Ana Perez». Email: «ana@mail.com». Teléfono: «1144445555». 2. `5 → 2`, suscriptor `2`, canales `1,3` (app móvil y SMS). 3. `5 → 3`, suscriptor `2`, club `2` (Boca Juniors). 4. `5 → 4` |
| Resultado esperado | Suscriptor creado. Canales habilitados: app móvil y SMS. Mensaje de que Ana Perez sigue a Boca Juniors. El listado muestra a Ana Perez con esos canales y ese club |
| Resultado obtenido | Se muestra «Suscriptor creado», «Canales habilitados: [App móvil, SMS]» y «Ana Perez ahora sigue a Boca Juniors». El listado muestra a Ana Perez con esos canales y ese club. |
| Estado | **OK** |

#### PF-19 – Registrar un patrocinio principal de un club

| Campo | Contenido |
|---|---|
| Caso de uso | CU11 |
| Objetivo | Que se pueda registrar un patrocinio con monto y fechas |
| Precondiciones | PF-01 ejecutado |
| Datos y pasos | `6 → 2`. Sponsor: `1` (Deportes SA). Club: `1` (River Plate). Monto: `500000`. Inicio: `2026-01-01`. Fin: `2026-12-31`. ¿Principal?: `s` |
| Resultado esperado | Mensaje de patrocinio registrado: Deportes SA – River Plate [principal], monto 500000, del 2026-01-01 al 2026-12-31. Figura en `6 → 4` |
| Resultado obtenido | Se muestra «Patrocinio registrado: Deportes SA -> River Plate [principal] $500000.0 (2026-01-01 - 2026-12-31)» y figura en el listado de patrocinios. |
| Estado | **OK** |

#### PF-20 – Segundo sponsor principal vigente para el mismo club (error)

| Campo | Contenido |
|---|---|
| Caso de uso | CU11 |
| Objetivo | Que un club no tenga dos sponsors principales al mismo tiempo |
| Precondiciones | PF-19 ejecutado |
| Datos y pasos | 1. `6 → 1`. Nombre: «Bebidas SA». Marca: «Refresh». Logo: «refresh.png». 2. `6 → 2`. Sponsor: `2` (Bebidas SA). Club: `1` (River Plate). Monto: `300000`. Inicio: `2026-06-01`. Fin: `2026-12-31`. ¿Principal?: `s` |
| Resultado esperado | El sistema informa que River Plate ya tiene un sponsor principal en esas fechas. El patrocinio no se registra |
| Resultado obtenido | Se crea el sponsor «Bebidas SA» y al patrocinar se muestra «River Plate ya tiene un sponsor principal en esas fechas». El patrocinio no se registra. |
| Estado | **OK** |

### Reportes y proyecciones (CU12, CU13)

#### PF-21 – Tabla de posiciones, goleadores y valla menos vencida

| Campo | Contenido |
|---|---|
| Caso de uso | CU12 |
| Objetivo | Que los reportes salgan bien con un partido terminado (3 puntos por ganar, solo cuentan los finalizados) |
| Precondiciones | PF-16 ejecutado (River Plate 1 – 0 Independiente, FINALIZADO). El gol anulado de PF-15 no debe contar |
| Datos y pasos | `7`, torneo `1`. Opciones `1`, `2` y `3` |
| Resultado esperado | **Tabla:** 1.º River Plate (PJ 1, G 1, GF 1, GC 0, DG 1, 3 puntos); Boca Juniors y Racing Club con 0 partidos y 0 puntos; Independiente (PJ 1, P 1, GF 0, GC 1, DG –1, 0 puntos). **Goleadores:** Jugador10 de River Plate con 1 gol. **Valla menos vencida:** River Plate |
| Resultado obtenido | Tabla: 1.º River Plate (PJ 1, G 1, GF 1, GC 0, DG 1, 3 pts); 2.º Boca Juniors y 3.º Racing Club con 0; 4.º Independiente (PJ 1, P 1, GF 0, GC 1, DG -1, 0 pts). Goleadores: Jugador10 de River con 1 gol (el anulado no cuenta). Valla menos vencida: River Plate. |
| Estado | **OK** |

#### PF-22 – Probabilidad de clasificación con los dos métodos

| Campo | Contenido |
|---|---|
| Caso de uso | CU13 |
| Objetivo | Que se pueda elegir entre los dos métodos de cálculo |
| Precondiciones | PF-21 ejecutado. Torneo con 2 cupos y puntaje de corte 30 |
| Datos y pasos | `7`, torneo `1`, opción `5`. 1. Club `1` (River Plate), método `1` (promedio de puntos por partido). 2. Repetir con club `1` y método `2` (simulación con xG promedio) |
| Resultado esperado | Método 1: 30,0 % (River Plate promedia 3 puntos por partido, proyecta 9 puntos en 3 partidos y el corte es 30). Método 2: un porcentaje entre 0 % y 100 %; al ser una simulación puede variar entre ejecuciones |
| Resultado obtenido | Método 1: «30.0% (Promedio de puntos por partido)». Método 2: «100.0% (Simulación con xG promedio)», dentro del rango esperado. |
| Estado | **OK** |

### Persistencia de datos

Acá probamos que no se pierda nada al cerrar. El programa carga `futbolstats.dat` al abrir (si existe) y lo guarda al salir con la opción `0`.

#### PF-23 – Guardar, cerrar y recuperar la información

| Campo | Contenido |
|---|---|
| Caso de uso | Persistencia |
| Objetivo | Que al volver a abrir el programa esté todo lo que habíamos cargado |
| Precondiciones | Sin archivo de datos previo |
| Datos y pasos | 1. Iniciar el programa y cargar los datos de demostración (`8`). 2. Programar el partido 1 como en PF-07. 3. Salir con `0`. 4. Volver a iniciar el programa. 5. Consultar el fixture (`3 → 3`, torneo `1`) |
| Resultado esperado | Al reiniciar, el torneo, los clubes y los jugadores siguen cargados sin repetir la demostración. El partido 1 figura PROGRAMADO el 2026-05-10 a las 18:00 en el Monumental, con su terna arbitral |
| Resultado obtenido | Al reiniciar se muestra «Datos recuperados correctamente.» El torneo, los clubes y los jugadores siguen cargados sin repetir la demostración, y el partido 1 figura PROGRAMADO. |
| Estado | **OK** |

#### PF-24 – Primer inicio sin archivo de datos

| Campo | Contenido |
|---|---|
| Caso de uso | Persistencia |
| Objetivo | Que el programa arranque bien aunque todavía no haya nada guardado |
| Precondiciones | Archivo de datos inexistente |
| Datos y pasos | Iniciar el programa y consultar el listado de torneos (`1 → 1`) |
| Resultado esperado | El programa inicia sin errores y sin datos cargados. El listado indica que no hay torneos |
| Resultado obtenido | Se muestra «No existe un archivo previo. Se inicia con datos vacíos.» El listado de torneos indica «(no hay datos cargados)». |
| Estado | **OK** |

#### PF-25 – Archivo de datos dañado

| Campo | Contenido |
|---|---|
| Caso de uso | Persistencia |
| Objetivo | Que si el archivo está roto, el programa avise y siga andando |
| Precondiciones | Archivo `futbolstats.dat` existente cuyo contenido se reemplazó por texto cualquiera |
| Datos y pasos | Iniciar el programa |
| Resultado esperado | El sistema informa con un mensaje claro que el archivo está dañado o no es válido. El programa no finaliza de forma abrupta y permite continuar |
| Resultado obtenido | Se muestra «No se pudieron cargar los datos: No se pudieron recuperar los datos de: futbolstats.dat» y «Se iniciará con datos vacíos.» El programa sigue funcionando normalmente. |
| Estado | **OK** |

### Modificación y baja de datos (CU01)

Estos dos van en una ejecución nueva, sin archivo guardado y después de cargar los datos de prueba (`8`).

#### PF-26 – Modificar datos con valores válidos e inválidos

| Campo | Contenido |
|---|---|
| Caso de uso | CU01 |
| Objetivo | Que se puedan modificar datos y que se rechacen los valores que no tienen sentido |
| Precondiciones | Datos de demostración cargados |
| Datos y pasos | 1. `1 → 8`, `Club`, club `1`, nueva sede: «Av. Figueroa Alcorta 7597». 2. `8`, `Jugador`, jugador `1`, número de camiseta `150`. 3. `8`, `Jugador`, jugador `1`, número `9`, valor de mercado `5000000`. 4. `8`, `Estadio`, estadio `1`, capacidad `-5` |
| Resultado esperado | Paso 1: se modifica la sede. Paso 2: se rechaza porque el número debe estar entre 1 y 99. Paso 3: se modifica. Paso 4: se rechaza porque la capacidad debe ser mayor a 0. Los datos rechazados no cambian |
| Resultado obtenido | Paso 1: «Listo.» Paso 2: «El numero de camiseta tiene que estar entre 1 y 99». Paso 3: «Listo.» Paso 4: «La capacidad tiene que ser mayor a 0». |
| Estado | **OK** |

#### PF-27 – Dar de baja datos en uso y libres

| Campo | Contenido |
|---|---|
| Caso de uso | CU01 |
| Objetivo | Que solo se pueda dar de baja lo que no se está usando |
| Precondiciones | Datos de demostración cargados |
| Datos y pasos | `1 → 9` y elegir, en orden: 1. `Club`, club `1` (River Plate). 2. `Entrenador`, entrenador `1`. 3. `Árbitro`, árbitro `9`. 4. `Estadio`, estadio `3`. 5. `Torneo`, torneo `1` (PLANIFICADO) |
| Resultado esperado | Pasos 1 y 2: se rechaza porque River Plate está inscripto en un torneo y su entrenador lo dirige. Pasos 3 a 5: se dan de baja (el árbitro y el estadio no están asignados a ningún partido y el torneo está PLANIFICADO) |
| Resultado obtenido | Paso 1: «River Plate esta inscripto en un torneo». Paso 2: «Entrenador1 Perez dirige a River Plate». Pasos 3, 4 y 5: «Listo.» |
| Estado | **OK** |
