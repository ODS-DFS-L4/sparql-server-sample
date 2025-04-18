#!/bin/sh
## Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

set -e

# Modify ADMIN_PASSWORD
if [ -f "$FUSEKI_DIR/shiro.ini" ] ; then
  cp "$FUSEKI_DIR/shiro.ini" "$FUSEKI_BASE/shiro.ini"
fi
if [ -n "$ADMIN_PASSWORD" ] ; then
  export ADMIN_PASSWORD
  envsubst '${ADMIN_PASSWORD}' < "$FUSEKI_BASE/shiro.ini" > "$FUSEKI_BASE/shiro.ini.$$" && \
    mv "$FUSEKI_BASE/shiro.ini.$$" "$FUSEKI_BASE/shiro.ini"
fi

# Link extra directory
if [ -d "/fuseki-extra" ] && [ ! -d "$FUSEKI_BASE/extra" ] ; then
  if [ -d "/fuseki-extra" ] ; then
    rm -rf /fuseki/extra
  fi
  ln -s "/fuseki-extra" "$FUSEKI_BASE/extra"
fi

exec "$@" &

echo "Wait until Fuseki finishes starting up..."
until $(curl --output /dev/null --silent --head --fail http://localhost:3030); do
  sleep 1s
done

# Create datasets
printenv | egrep "^FUSEKI_DATASET_" | while read env_var
do
    dataset=$(echo $env_var | egrep -o "=.*$" | sed 's/^=//g')
    echo "Creating dataset $dataset"
    curl -s 'http://localhost:3030/$/datasets'\
         -u admin:${ADMIN_PASSWORD}\
         -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8'\
         --data "dbName=${dataset}&dbType=tdb2"

    # Load all TTL files from /datasets/{dataset name} directory into the dataset
    if [ -d "$FUSEKI_DIR/datasets/$dataset" ]; then
        echo "Loading data into dataset $dataset from $FUSEKI_DIR/datasets/$dataset/*.ttl"
        for ttl_file in $FUSEKI_DIR/datasets/$dataset/*.ttl; do
            if [ -f "$ttl_file" ]; then
                echo "Loading $ttl_file into dataset $dataset"
                curl -X POST -u admin:${ADMIN_PASSWORD} \
                    -H 'Content-Type: text/turtle' \
                    --data-binary "@$ttl_file" \
                    "http://localhost:3030/${dataset}/data"
            fi
        done
    fi
done

unset ADMIN_PASSWORD

wait
