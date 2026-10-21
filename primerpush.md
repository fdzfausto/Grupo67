# ***Trabajo Práctico Obligatorio Programación Orientada a Objetos***

## Presentación

FutbolStats es una plataforma para administrar torneos de fútbol profesional, registrar partidos en vivo, conservar estadísticas, notificar hechos relevantes y producir reportes y proyecciones. Este documento desarrolla la Etapa 1: análisis del problema, alcance funcional, entidades, actores, requerimientos funcionales, reglas de negocio, supuestos, diagrama de casos de uso, casos de uso, diagrama de clases inicial, diagrama de secuencia, aplicación de SOLID y GRASP y diagrama de clases final.

## Alcance funcional

Administrar torneos, clubes, estadios, jugadores, entrenadores y árbitros. Inscribir clubes con plantel ya armado y generar el fixture del torneo. Programar partidos con local/visitante, fechayhora, estadio y arbitros. Registrar eventos durante el juego y cerrar el estado del partido. Notificar goles, tarjetas rojas/amarillas. Gestionar contratos de jugadores y patrocinios. Consultar tabla de posiciones, goleadores y **¿valla menos vencida. Calcular xG y probabilidad de clasificación mediante estrategias intercambiables?**

## Identificador de entidades

* **Empresa: ¿que va aca?**  
* Fixture: organiza los partidos de un torneo.  
* Estadios:  
* Planteles: Plantel por inscripción de un club a un torneo  
* Árbitros: grupo de arbitros designados a un partido  
* Partidos: class “Partido”  
* Eventos: abstract \- Evento: Gol, Remate, Tarjeta,Cambio y Falta.  
* Persona Clase base para Jugador, Entrenador y Arbitro.  
* Clubes: class “club”  
* Entrenadores: class “entrenador”  
* Jugadores: class “jugador”  
* Sponsor: clase “sponsor”  
* **Patrocinio ¿que va aca?**  
* Suscriptor: class “subscroptor”, con clubes seguidos con  
* Canales (enum): enum “Canal”  
* **Analista: ¿que va aca?**  
* Notificaciones: class “Notificacion”

## Actores del sistema

Administrado: da de alta y mantiene torneos, clubes, estadios, personas, inscripciones, contratos y patrocinios, genera el fixture y programa partidos.  
Operador de partido: Inicia el partido, carga eventos en vivo, modifica eventos y finaliza el partido.  
Analista: ¿?  
Suscriptor: configura los clubes que sigue y los canales de que desea recibir avisos/eventos  
“Actor” ….

## Requerimientos funcionales

RF01 \- Inscribir un club en un torneo con su plantel \- CU\*\*  
RF02 \- Administrar torneos \- CU\*\*  
RF03 \- Administrar clubes \- CU\*\*  
RF04 \- Administrar estadios \- CU\*\*  
RF05 \- Generar fixture \- CU\*\*  
RF06  
RF07  
RF08  
RF09  
RF10  
RF11  
RF12  
RF13  
RF14  
RF15  
RF16  
RF17  
RF18

## Reglas de Negocio

RN01 \- Estados del torneo \- PLANIFICADO, ENCURSO y FINALIZADO. Solo se permite avanzar al estado siguiente.  
RN02 \- Inicio del torneo \- Un torneo requiere al menos 4 clubes inscriptos para que comience  
RN03 \- Inscripcion \- No se inscribe un club mas de una vez al mismo torneo y no se  
permiten nuevas inscripciones cuando el torneo está ENCURSO  
RN04 \- Fechas \- La fecha y hora de cada partido deben quedar dentro del período de su torneo.  
RN05 \- Plantel \- Cada plantel de una inscripción contiene entre \*\* y \*\* jugadores  
RN06 \- Pertenencia \- Un jugador solo puede pertenecer a un club por vez dentro del  
mismo torneo.  
RN07 \-   
RN08 \- Disponibilidad \- Un estadio no tiene que admitir partidos con menos de 3 horas entre sí  
RN09 \- Programacion \- Un club no puede jugar dos partidos con menos de 48 horas de diferencia  
RN10 \- Árbitros \- Un partido requiere un árbitro principal y dos asistentes.  
RN11 \- Estado del partido \- PROGRAMADO, ENJUEGO, FINALIZADO y SUSPENDIDO.  
RN12 \-   
RN13 \- Tiempo de evento \- El tiempo de partido es entre 0 y 120 minutos, puede incluir tiempo adicional.  
RN14 \- Gol y marcador \- Un gol puede ser a favor, en contra o de penal. El gol en contra suma al marcador del rival el gol de penal cuenta para el goleador si es a favor.  
RN15 \- Tarjetas \- Dos amarillas en el mismo jugador en un partido equivalen a una roja. Un expulsado no puede ser sustituido y el club queda con un jugador menos.  
RN16 \- Cambios \- Cada club puede realizar como max 5 sustituciones por partido.  
RN17 \-   
RN18 \-   
RN19 \-   
RN20 \- 

## Supuestos 

S01 \-   
S02 \-   
S03 \-   
S04 \-   
S05 \- 

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

