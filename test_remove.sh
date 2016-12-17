#!/bin/sh

mongo xkcd --eval "db.comics.findAndModify({query :{}, sort: {"_id" : -1}, remove:true})"
sbt run
