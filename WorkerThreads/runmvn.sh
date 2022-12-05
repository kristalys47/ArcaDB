#!/usr/bin/env bash
export $(grep -v '^#' .env | xargs)
# set -a # automatically export all variables
# source .env
# set +a
mvn exec:java -Dexec.mainClass="orc.main" -Dexec.args="${S3_BUCKET} ${AWS_S3_ACCESS_KEY} ${AWS_S3_SECRET_KEY} ${REDIS_HOST} ${REDIS_PORT} ${REDIS_HOST_TIMES} ${REDIS_PORT_TIMES} ${WORKER_APP_PORT} ${COORDINATOR_APP_PORT} ${POSTGRES_PASSWORD} ${POSTGRES_USERNAME} ${POSTGRES_HOST} ${POSTGRES_PORT} ${POSTGRES_DB_NAME} ${MODE}" #-Dexec.cleanupDaemonThreads=false -Dsbt.classloader.close=false --log-file "logs.log"
