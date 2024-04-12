#!/bin/bash

StackName="queryx-fn"
SecretId="arn:aws:secretsmanager:us-east-1:932529686876:secret:rds!cluster-c2c4ea32-10d9-4f83-8da4-62be31ec8186-AjNX1X"
JDBCURL="jdbc:postgresql://tf-2024041210325356990000000a.cluster-c9vpztgbbztw.us-east-1.rds.amazonaws.com/sqldb"
Queries="SELECT 2+2"
Queries64=$(echo -n $Queries | base64)
SecurityGroupIds="sg-0623b36abfba1ff90"
SubnetIds="subnet-0543dda2264e0092d"

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
