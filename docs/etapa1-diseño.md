# ***Trabajo Práctico Obligatorio Programación Orientada a Objetos***

## Presentación

FutbolStats es una plataforma para administrar torneos de fútbol profesional, registrar partidos en vivo, conservar estadísticas, notificar hechos relevantes y producir reportes y proyecciones. Este documento desarrolla la Etapa 1: análisis del problema, alcance funcional, entidades, actores, requerimientos funcionales, reglas de negocio, supuestos, diagrama de casos de uso, casos de uso, diagrama de clases inicial, diagrama de secuencia, aplicación de SOLID y GRASP y diagrama de clases final.

## Alcance funcional

Administrar torneos, clubes, estadios, jugadores, entrenadores y árbitros. Inscribir clubes con plantel ya armado y generar el fixture del torneo. Programar partidos con local/visitante, fechayhora, estadio y árbitros. Registrar eventos durante el juego y cerrar el estado del partido. Notificar goles, tarjetas rojas/amarillas. Gestionar contratos de jugadores y patrocinios. Consultar tabla de posiciones, goleadores y **¿valla menos vencida. Calcular xG y probabilidad de clasificación mediante estrategias intercambiables?**

## Actores del sistema

| Actor  | Qué hace en el sistema |
| :---: | :---: |
| Administrador | Carga y mantiene torneos, clubes, estadios, jugadores, entrenadores y árbitros. Inscribe clubes, genera el fixture, programa partidos y gestiona contratos y patrocinios. |
| Operador del partido | Carga las nóminas, inicia el partido, registra los eventos en vivo, anula los eventos erróneos y cierra el partido.  |
| Analista  | Consulta la tabla de posiciones, los goleadores y la valla menos vencida, y pide proyecciones de xG y de probabilidad de clasificación.  |
| Suscriptor | Se registra, elige qué clubes sigue y por qué canales, y recibe las notificaciones.  |
| Canales de notificación  | Entregan cada aviso al suscriptor. Pueden fallar sin frenar la carga del partido.  |

## Requerimientos funcionales

* **RF01:** Administrar torneos (alta, modificación, consulta y baja) y avanzar su estado: planificado, en curso, finalizado. **\- Administrador \-**  
* **RF02:** Administrar clubes (alta, modificación, consulta y baja) y su cuerpo técnico. **\- Administrador \-**   
* **RF03**: Administrar estadios (alta, modificación, consulta y baja), con su dirección y condiciones. **\- Administrador \-**  
* **RF04:** Administrar jugadores (alta, modificación, consulta y baja), con posición, número de camiseta y valor de mercado. **\- Administrador \-**  
* **RF05:** Administrar entrenadores (alta, modificación, consulta y baja), con especialidad y licencia. **\- Administrador \-**  
* **RF06:** Administrar árbitros (alta, modificación, consulta y baja), con categoría, colegio y clubes de los que es hincha. **\- Administrador \-**   
* **RF07:** Gestionar los contratos de los jugadores (alta, transferencia y rescisión), conservando el historial de clubes. **\- Administrador \-**  
* **RF08:** Inscribir un club en un torneo con su plantel. **\- Administrador \-**  
* **RF09:** Generar el fixture del torneo (todos contra todos) a partir de los clubes inscriptos. **\- Administrador \-**  
* **RF10:** Programar un partido con fecha y hora, estadio y terna arbitral, validando que no haya superposiciones. **\- Administrador \-**  
* **RF11:** Cargar la nómina de cada club para un partido, con titulares y suplentes y sin jugadores suspendidos. **\- Operador del partido** \-   
* **RF12:** Iniciar el partido, registrar sus eventos en vivo (goles, remates, tarjetas, cambios y faltas) y cerrar el partido. **\- Operador del partido \-**  
* **RF13:** Anular un evento cargado por error sin borrarlo. **\- Operador del partido \-**   
* **RF14:** Notificar goles, tarjetas (amarillas y rojas) y finales de partido a los suscriptores, por los canales que cada uno eligió. **\- Sistema → Suscriptores y Canales \-**   
* **RF15:** Registrarse y elegir los clubes que sigue y los canales por los que recibe los avisos. **\- Suscriptor \-**   
* **RF16:** Registrar patrocinios y sponsors a clubes y torneos. **\- Administrador \-**  
* **RF17:** Emitir la tabla de posiciones, la lista de goleadores y la valla menos vencida de un torneo. **\- Analista \-**   
* **RF18:** Calcular proyecciones: goles esperados (xG) y probabilidad de clasificación según el método que elija. **\- Analista \-**

## Entidades

* Fixture: organiza los partidos de un torneo.  
* Estadios:  
* Planteles: Plantel por inscripción de un club a un torneo  
* Árbitros: grupo de árbitros designados a un partido  
* Partidos: class “Partido”  
* Eventos: abstract \- Evento: Gol, Remate, Tarjeta,Cambio y Falta.  
* Persona Clase base para Jugador, Entrenador y Árbitro.  
* Clubes: class “club”  
* Entrenadores: class “entrenador”  
* Jugadores: class “jugador”  
* Sponsor: clase “sponsor”  
* **Patrocinio ¿que va aca?**  
* Suscriptor: class “suscriptor”, con clubes seguidos con  
* Canales (enum): enum “Canal”  
* **Analista: ¿que va aca?**  
* Notificaciones: class “Notificacion”

## Reglas de Negocio

* **RN01** \- Estados del torneo \- PLANIFICADO, EN CURSO y FINALIZADO. Solo se permite avanzar al estado siguiente.  
* **RN02** \- Inicio del torneo \- Un torneo requiere al menos 4 clubes inscriptos para que comience  
* **RN03** \- Inscripción \- Un club no puede inscribirse más de una vez en el mismo torneo, y no se permiten nuevas inscripciones cuando el torneo esta EN CURSO. 	  
* **RN04** \- Fechas \- La fecha y hora de cada partido deben quedar dentro del período de su torneo.  
* **RN05** \- Plantel \- Cada plantel de una inscripción contiene entre \*\* y \*\* jugadores  
* **RN06** \- Pertenencia \- Un jugador solo puede pertenecer a un club por vez dentro del mismo torneo.  
* **RN07** \- Historial del jugador \- Los datos y estadísticas de un jugador se conservan aunque rescinda su contrato o quede libre. Cada paso por un club queda registrado con su fecha de inicio y fin.  
* **RN08** \- Disponibilidad del estadio \- Un estadio no tiene que admitir partidos con menos de 3 horas de diferencia.  
* **RN09** \- Programación \- Un club no puede jugar dos partidos con menos de 48 horas de diferencia.  
* **RN10** \- Árbitros \- Un partido requiere un árbitro principal y dos asistentes.  
* **RN11** \- Estado del partido \- PROGRAMADO, EN JUEGO, FINALIZADO y SUSPENDIDO.  
* **RN12** \- Registro de eventos \- Solo se pueden registrar eventos con el partido EN JUEGO. El jugador involucrado debe estar en la nómina de su club para ese partido.   
* **RN13** \- Tiempo de evento \- El tiempo de partido es entre 0 y 120 minutos (incluido el tiempo adicional).  
* **RN14** \- Gol y marcador \- Un gol puede ser a favor, en contra o de penal. El gol en contra suma al marcador del rival mientras que el gol de penal cuenta para el goleador si es a favor.  
* **RN15** \- Tarjetas \- Dos amarillas en el mismo jugador en un partido equivalen a una roja. Un expulsado no puede ser sustituido y el club queda con un jugador menos.  
* **RN16** \- Cambios \- Cada club puede realizar como máximo 5 sustituciones por partido. Un jugador sustituido no puede volver a entrar   
* **RN17** \- Anulación de eventos \- Un evento registrado no se elimina. Si hubo un error, se corrige con un evento de anulación asociado al original.  
* **RN18** \- Sanciones \- Un jugador que acumula 5 tarjetas amarillas en el torneo queda suspendido para la fecha siguiente, y una roja directa implica 1 fecha de suspensión. Un jugador suspendido no puede incluirse en la nómina.  
* **RN19** \- Impedimentos del arbitro \- Un árbitro no puede dirigir a un club que es hincha declarado ni tener otro partido el mismo día.   
* **RN20** \- Notificaciones \- Cada gol, tarjeta y final del partido se notifica a los suscriptores que siguen a algunos de los clubes, por los canales que eligieron, indicando partido, minuto y hecho.  
* **RN21** \- Fallo de canales \- Si un canal falla, se registra el error y se sigue con los demás, sin frenar la carga del partido. Un canal nuevo debe poder agregarse sin modificar la lógica de los partidos.   
* **RN22** \- Patrocinios \- Un sponsor puede patrocinar varios clubes y torneos a la vez. Cada patrocinio tiene monto, fecha de inicio y fin, y solo puede haber uno vigente por sponsor y destinatario.  
* **RN23** \- Sponsor principal \- Un club no puede tener más de un sponsor principal vigente a la vez.  
* **RN24** \- Tabla de posiciones \- La victoria suma 3 puntos, el empate 1 y la derrota 0, solo cuentan los partidos finalizados.  
* **RN25** \- Criterio de desempate en tabla de posiciones \- En caso de empate en puntos, la tabla de posiciones se ordenará aplicando primero la diferencia de gol, luego los goles a favor y finalmente el resultado directo entre los equipos involucrados.  
* **RN26** \- Goleador y Valla menos vencida \- El goleador es el jugador con mas goles a favor: los de penal cuentan y los en contra no. La valla menos vencida es el club con el menor promedio de goles recibidos por partido.  
* **RN27** \- Goles esperados (xG) \- Cada remate vale según la zona desde la que se patea: área chica 0.40, área grande 0.15, fuera del área 0.04 y penal 0.76. El xG de un jugador o de un club en un partido es la suma de sus remates.   
* **RN28** \- Probabilidad de clasificación \- recen al menos dos métodos intercambiables: comparar los puntos proyectados con el corte de años anteriores, o simular los partidos restantes con el xG promedio de cada club. Se calcula al pedirla, no se guarda, y un método nuevo se agrega sin modificar los reportes que lo usan. 

## Supuestos 

S01 \- Actores \- El enunciado no dice quien carga los datos, organiza el torneo ni programa los partidos: el actor “Administrador” será encargado de eso. Los eventos del partido los registra el “Operador del Partido”.  
S02 \- Datos del torneo \- Cada torneo indica si es nacional o internacional, cuantos clubes clasifican y el puntaje de corte de años anteriores. Los carga el “Administrador” al crear el torneo.  
S03 \- Fixture \- Se genera todos contra todos a una sola rueda. Si la cantidad de clubes es impar, en cada fecha un club queda libre. El fixture define los cruces y la localía; el estadio, la hora y la terna se asignan al programar cada partido.   
S04 \- Nómina \- Antes de iniciar el partido, cada club presenta 11 titulares y hasta 7 suplentes, elegidos de su plantel inscripto en el torneo.   
S05 \- Sanciones \- “Fecha siguiente” se interpreta como el próximo partido del club en ese torneo. Una expulsión por doble amarilla se trata como roja: 1 fecha de suspensión, y se notifica como tarjeta roja.   
S06 \- Desempate \- “Resultado entre los clubes empatados” son los puntos que sacaron en los partidos entre ellos. Si siguen empatados o no se enfrentaron, se ordenan alfabéticamente.  
S07 \- Proyección por promedio \- Los puntos proyectados son el promedio de puntos por partido multiplicado por la cantidad total de partidos del club. Si alcanzan el corte, la probabilidad es 100 %; si no, es el porcentaje que representan del corte.

S08 \- Proyección por xG \- Se simulan los partidos que faltan 1.000 veces, usando el xG promedio de cada club para estimar sus goles. La probabilidad es el porcentaje de simulaciones en las que el club termina entre los clasificados.

## Diagrama de Casos de Uso

| FutbolStats  |
| :---: |

## Casos de Uso

| CU01 |  |
| :---- | :---- |
| Nombre | Generar Fixture |
| Actor | Administrador |
| Precondición |  |
| Flujo Principal |  |
| Flujo alternativo |  |

plantilla

| CU\*\* |  |
| :---- | :---- |
| Nombre |  |
| Actor |  |
| Precondición |  |
| Flujo Principal |  |
| Flujo alternativo |  |

## Diagrama de Clases Inicial

|  |
| :---: |

## Diagrama de Secuencia

|  |
| :---: |

## Aplicación de principios SOLID

## 

## Aplicación de principios GRASP

## Diagrama de Clases Final

|  |
| :---: |

## 

