#!/bin/bash

echo "Creating DynamoDB table..."

awslocal dynamodb create-table \
  --table-name UserScores \
  --attribute-definitions \
    AttributeName=userId,AttributeType=S \
  --key-schema \
    AttributeName=userId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1

echo "DynamoDB table created successfully"