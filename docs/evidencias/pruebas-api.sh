#!/usr/bin/env bash
# Ejecuta los escenarios de la Actividad 6 contra la API en ejecución.
# Uso: ./docs/evidencias/pruebas-api.sh [puerto]   (por defecto 8080)
B="http://localhost:${1:-8080}/api/prestamos"

echo "Evidencia: pruebas manuales de la API con curl (app en ejecución con H2 y data.sql)"
echo "Fecha: $(date +%F)"
echo

run() {
  echo "=================================================================="
  echo "ESCENARIO: $1"
  echo "ESPERADO : $2"
  shift 2
  echo "\$ curl $*"
  echo
  curl -s -i "$@" | tr -d '\r' | grep -vE '^(Date|Keep-Alive|Connection|Transfer-Encoding|Vary):'
  echo
  echo
}

J='Content-Type: application/json'
run "Préstamo de libro disponible (estudiante 1, libro 2)" "201 Created y préstamo registrado" \
    -X POST "$B" -H "$J" -d '{"estudianteId":1,"libroId":2}'
run "Libro ya prestado (libro 3 tiene préstamo activo)" "409 Conflict; sin nuevo préstamo" \
    -X POST "$B" -H "$J" -d '{"estudianteId":1,"libroId":3}'
run "Libro recién prestado (libro 2 otra vez)" "409 Conflict (R3: ya no está disponible)" \
    -X POST "$B" -H "$J" -d '{"estudianteId":3,"libroId":2}'
run "Estudiante inexistente" "404 Not Found" \
    -X POST "$B" -H "$J" -d '{"estudianteId":999,"libroId":1}'
run "Libro inexistente" "404 Not Found" \
    -X POST "$B" -H "$J" -d '{"estudianteId":1,"libroId":999}'
run "Solicitud inválida (R4)" "400 Bad Request; sin préstamo" \
    -X POST "$B" -H "$J" -d '{"estudianteId":null,"libroId":-1}'
run "Consulta de préstamos" "200 OK con registros (3: 2 iniciales + 1 creado; los 409/404/400 no crearon nada)" \
    "$B"
