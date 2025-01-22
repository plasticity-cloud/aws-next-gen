
package cloud.plasticity;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.nio.spi.s3.CacheableS3Client;
import java.nio.file.spi.FileSystemProvider;
import software.amazon.nio.spi.s3.S3FileSystemProvider;

/**
 * The module containing all dependencies required by the {@link S3FileHandler}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of S3AsyncClient
     */
    public static S3AsyncClient s3Client() {

        System.out.println(FileSystemProvider.installedProviders().toString());
	new S3FileSystemProvider();
	System.out.println(FileSystemProvider.installedProviders().toString());
        return new CacheableS3Client(S3AsyncClient.builder()
                       .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                       .region(Region.US_EAST_1)
                       .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
                       .build());
    }
}
