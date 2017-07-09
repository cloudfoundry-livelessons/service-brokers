#!/bin/bash

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D | ./clean-routes.py
}

app_domain mailgun-service-broker
