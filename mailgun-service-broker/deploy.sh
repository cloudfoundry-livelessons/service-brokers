#!/bin/bash

service_broker=mailgun-service-broker
service_broker_db=${service_broker}-db

./mvnw -DskipTests=true clean install


cf create-service cleardb spark ${service_broker_db};

cf push -p target/${service_broker}.jar --no-start ${service_broker}
cf bind-service ${service_broker} ${service_broker_db}

cf set-env ${service_broker} MAILGUN_API_KEY ${MAILGUN_API_KEY}
cf set-env ${service_broker} MAILGUN_DOMAIN ${MAILGUN_DOMAIN}
cf start ${service_broker}

uri=http://mailgun-service-broker.cfapps.io/
cf delete-service-broker -f ${service_broker}
cf create-service-broker ${service_broker} admin admin $uri --space-scoped
