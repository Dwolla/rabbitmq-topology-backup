#!/usr/bin/env bash
set -o errexit -o pipefail
IFS=$'\n\t'

STAGE="$1"
if [ -z "${STAGE}" ]; then
  echo "Error: missing deploy environment. Pass it as the first argument to the script." > /dev/stderr
  exit 1
fi

set -o nounset

# nvm is a bash function, so fake command echoing for nvm commands to reduce noise
echo "+ . "${NVM_DIR}/nvm.sh" --no-use"
. "${NVM_DIR}/nvm.sh" --no-use

echo "+ nvm install"
nvm install

set -o xtrace
npm install -g npm
npm install -g serverless
serverless deploy \
  --region us-west-2 \
  --stage "$STAGE"
