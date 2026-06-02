#!/bin/bash
# smime-generator.sh
thisDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# app="$(basename "$thisDir")"
# ###########################################################################################
#
# up() {(
#   set -euo pipefail
#   # #############################################################
#   cd "$thisDir"
#   bun run build && bun run dist
#   # #############################################################
#   aws_load_profile
#   aws s3 sync "$thisDir/dist/" "s3://$KN_CBS_BUCKET/apps/$app/dist/" --delete
# )}

run() {(
  set -euo pipefail
  # #############################################################
  cd "$thisDir"
  java -jar target/smime-generator.jar "$@"
)}
# ###########################################################################################
# ###########################################################################################
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  func="${1:-run}"
  shift
  "$func" "$@"
fi