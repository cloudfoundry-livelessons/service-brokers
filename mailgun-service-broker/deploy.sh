#!/bin/bash

root=$(cd `dirname $0` && pwd)
service_broker=mailgun-service-broker
service_broker_db=${service_broker}-db

function reset(){
    cf d -f ${service_broker}
    cf ds -f ${service_broker_db}
#    cf purge-service-instance -f ${service_broker}
    cf delete-service-broker -f ${service_broker}
    cf purge-service-offering ${service_broker} -f
}

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D | ./clean-routes.py
}


function create_db (){
   cf marketplace | grep cleardb && cf create-service cleardb spark ${service_broker_db};
}

function deploy_service_broker_app(){
    ./mvnw -DskipTests=true clean install
    cf s | grep ${service_broker_db} || create_db
    echo "created a DB: $service_broker_db"
    cf push -p target/${service_broker}.jar --no-start ${service_broker}
    cf set-env ${service_broker} MAILGUN_API_KEY ${MAILGUN_API_KEY}
    cf set-env ${service_broker} MAILGUN_DOMAIN ${MAILGUN_DOMAIN}
    cf start ${service_broker}
}

function configure_service_broker(){
    uri=`app_domain ${service_broker}`
    uri=http://${uri}
    echo $uri
    cf create-service-broker ${service_broker} admin admin $uri --space-scoped
#    cf enable-service-access ${service_broker} -p basic
}

reset
deploy_service_broker_app
configure_service_broker
