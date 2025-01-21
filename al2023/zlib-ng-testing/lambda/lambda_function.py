from typing import Dict

from aws_lambda_powertools.utilities.streaming.s3_object import S3Object
from aws_lambda_powertools.utilities.typing import LambdaContext


def lambda_handler(event: Dict[str, str], context: LambdaContext):
    s3 = S3Object(bucket=event["bucket"], key=event["key"], is_gzip=True, is_csv=False)
    dateCounter=0
    lineCounter=0
    for line in s3:
      dateColumn=line[:9]
      lineCounter += 1
      print str(dateColumn)
      if str(dateColumn).startswith("WARC-Date"):
        dateCounter += 1
        print(f"Already found {dateCounter}")
      if lineCounter == 50000
        break
    print (f"Number of date objects {dateCounter}")
