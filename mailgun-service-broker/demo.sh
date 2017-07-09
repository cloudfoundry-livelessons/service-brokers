#!/bin/bash

function go(){
    cf  d -f m-sample-app
    cf ds -f m-mailgun
    ./deploy.sh
    cf delete-orphaned-routes -f

    cd demo
    spring jar hi.jar hi.groovy
    cf push -p hi.jar m-sample-app --no-start
    cd ..
    cf cs mailgun-service-broker basic m-mailgun
    cf bs m-sample-app m-mailgun -c '{ "filter":"read-only","action": "an-action","permissions": "read-only"}'

}



go