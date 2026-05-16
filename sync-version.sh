#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Uso: $0 <version>"
  exit 1
fi

VER="$1"

echo "Actualizando pom.xml a la version ${VER}..."

cd messenger
./mvnw versions:set -DnewVersion="${VER}" -DgenerateBackupPoms=false
if [ $? -ne 0 ]; then
  echo "Error al actualizar la version de Maven." >&2
  cd ..
  exit 1
fi
cd ..

echo "Actualizando archivos de documentacion..."

DocsToUpdate=("README.md" "README.en.md")

for File in "${DocsToUpdate[@]}"; do
  if [ -f "$File" ]; then
    # Reemplaza Version-x.y.z o Version-x.y.z-SNAPSHOT solo en las lineas que contienen alt="Version"
    sed -E "/alt=[\"']Version[\"']/{s/Version-[0-9]+(\.[0-9]+)*(-SNAPSHOT)?/Version-${VER}/g}" "$File" > "$File.tmp" && mv "$File.tmp" "$File"
    echo "$File actualizado."
  fi
done

echo -e "\033[32mVersion actualizada con exito a ${VER} en todos los archivos centralizados.\033[0m"
echo -e "\033[33mNota: La version de Swagger/OpenAPI se actualizara automaticamente en la proxima compilacion.\033[0m"
