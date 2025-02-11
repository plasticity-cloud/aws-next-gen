In order to build separate Lambda image, with zlib from base operating system and with zlib-ng-compat package,
please invoke in your own build environment


# Authenticate to your private ECR
# Example for Lambda deployed in ap-southeast-1, where genomics data is available
# aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 864899852480.dkr.ecr.ap-southeast-1.amazonaws.com

1. edit file alternative_runtime_build_lambda_java.sh and update your region and account id,
respectively AWS_REGION and AWS_ACCOUNT variables.

2. in order to build code and images and push to ECR

./alternative_runtime_build_lambda_java.sh s3-files-parser zlib-base

./alternative_runtime_build_lambda_java.sh s3-files-parser zlib-ng
