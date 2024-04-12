#!/bin/bash

echo "StackName=$StackName"
echo "SecretId=$SecretId"
echo "JDBCURL=$JDBCURL"
echo "Queries64=$Queries64"
echo "SecurityGroupIds=$SecurityGroupIds"
echo "SubnetIds=$SubnetIds"

aws sts get-caller-identity
sleep 15

sam deploy --template-file template.sam.yaml \
  --stack-name $StackName \
  --capabilities CAPABILITY_IAM \
  --resolve-s3 \
  --parameter-overrides \
      ParameterKey=SecretId,ParameterValue=$SecretId \
      ParameterKey=JDBCURL,ParameterValue=$JDBCURL \
      ParameterKey=Queries64,ParameterValue=$Queries64 \
      ParameterKey=SecurityGroupIds,ParameterValue=$SecurityGroupIds \
      ParameterKey=SubnetIds,ParameterValue=$SubnetIds

echo "deploy done"
