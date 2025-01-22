package cloud.plasticity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.nio.spi.s3.S3FileSystemProvider;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class S3FileHandler implements RequestHandler<S3FileSearchRecord,String> {
    private final S3AsyncClient s3Client;
    //private final S3FileSystemProvider s3FileSystemProvider;

    public S3FileHandler() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    @Override
    public String handleRequest(S3FileSearchRecord record, final Context context) {
        
	
        S3FileParserHandler s3ParserHandler = new S3FileParserHandler();
        return s3ParserHandler.parse(record.bucket(), record.path(), record.searchKeyword());
    }
}

record S3FileSearchRecord(String bucket, String path, String searchKeyword) {
}
