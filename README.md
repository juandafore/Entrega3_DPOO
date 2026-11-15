# Club Deportivo Séneca (CDS) – Proyecto 2

Sistema del Club Deportivo Séneca con interfaz de consola para tres tipos de usuario,
torneos internos (recreativos y competitivos) y pruebas automáticas con JUnit 5.

## Requisitos
- Java 11 o superior.
- Eclipse con JUnit 5 (incluido en Eclipse IDE for Java Developers).

## Abrir en Eclipse
1. `File → Import → General → Existing Projects into Workspace`.
2. Seleccionar la carpeta `ClubDeportivoSeneca`.
3. El proyecto tiene dos carpetas de fuentes: `src` (aplicación) y `test` (pruebas). La librería JUnit 5 ya está en el *Build Path*.

## Ejecutar las consolas (un `main` por tipo de usuario)
Clic derecho sobre la clase → `Run As → Java Application`:

| Consola | Clase | Usuarios de prueba (login / password) |
|---|---|---|
| Jugadores (socios, estudiantes, externos) | `consola.ConsolaJugador` | `juan/juan`, `maria/maria`, `pedro/pedro`, `laura/laura`, `tomas/tomas`, `leon1/leon1`…; estudiante `sofia/sofia`; externo `andres/andres` |
| Empleados | `consola.ConsolaEmpleado` | entrenadores `carlos/carlos`, `diego/diego`; fisioterapeuta `fabiola/fabiola` |
| Administrador | `consola.ConsolaAdministrador` | `admin/admin` |

- Los datos se cargan automáticamente de `datos/club.dat` y se guardan en ese mismo archivo al salir (opción 0 del menú de inicio). La carpeta `datos/` está fuera de `src/`.
- Si `datos/` no existe se crean datos de prueba (equipos completos, academias con ranking, turnos de lunes a viernes, cinco torneos de ejemplo, uno de ellos finalizado). Para reiniciar, borre la carpeta `datos/`.
- La carpeta de trabajo al ejecutar debe ser `ClubDeportivoSeneca` (es la predeterminada en Eclipse).

Escenarios sugeridos:
- `maria` tiene un bono ganado en el torneo finalizado: úselo en *Comprar en la cafetería* o *Comprar en la tienda*.
- `fabiola` puede inscribirse en la *Copa Recreativa de Tenis* (sábado); `carlos` es rechazado en el torneo de tenis de mesa del miércoles porque está de turno.
- Como `admin`: *Torneos internos → Inscribir un equipo* (Leones FC y Halcones FC), *Iniciar torneo* y *Registrar resultado*.

## Ejecutar las pruebas
Clic derecho sobre la carpeta `test` → `Run As → JUnit Test` (127 pruebas: unitarias en `pruebas.unitarias`, integración por historia de usuario en `pruebas.integracion`).

## Desde la terminal (opcional)
```bash
mkdir -p bin && javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin consola.ConsolaAdministrador
```

## Estructura
```
src/modelo/          lógica de negocio (torneos en modelo/torneos)
src/persistencia/    guardado/carga
src/consola/         interfaz de consola (ConsolaBase, Entrada, Menu, Acciones*)
src/main/            DatosIniciales y programas de demostración del Proyecto 1
test/pruebas/        Fixtures, pruebas unitarias e integración
```
# Entrega3_DPOO
