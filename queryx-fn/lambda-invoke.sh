#!/bin/bash
StackName="queryx-fn"

# Function to get the ARN from CloudFormation and invoke the Lambda function
invoke_cloudformation_lambda() {
    # Parameters
    local stack_name="$1"  # Name of the CloudFormation stack
    local output_key="$2"  # The key of the output where the Lambda ARN is stored

    # Get the ARN of the Lambda function from CloudFormation stack outputs
    lambda_arn=$(aws cloudformation describe-stacks --stack-name "$stack_name" \
        --query "Stacks[0].Outputs[?OutputKey=='$output_key'].OutputValue" \
        --output text)

    # Check if the Lambda ARN was retrieved
    if [ -z "$lambda_arn" ]; then
        echo "Failed to retrieve Lambda ARN from CloudFormation stack"
        return 1
    fi

    echo "Retrieved Lambda ARN: $lambda_arn"

    # Invoke the Lambda function using the retrieved ARN
    response=$(aws lambda invoke \
        --function-name "$lambda_arn" \
        --payload '{}' \
        --cli-binary-format raw-in-base64-out \
        outputfile.txt)

    echo "Lambda function invoked. Response:"
    cat outputfile.txt
    echo  # Print a newline
}

# Example usage
stack_name="$StackName"
output_key="QueryxFnARN"
invoke_cloudformation_lambda "$stack_name" "$output_key"
