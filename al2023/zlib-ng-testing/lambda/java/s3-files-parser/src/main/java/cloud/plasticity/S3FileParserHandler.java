package cloud.plasticity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3FileParserHandler {

	// private LambdaLogger logger = context.getLogger();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String NON_MATCHED_FILE_NAME = "NON_MATCHED.gz";
	private final String MATCHED_FILE_NAME = "MATCHED.gz";
	private final String EPHEMERAL_STORAGE_LOCATION_KEY = "ephemeral-location";
	private final String RATE_LIMITING_KEY = "RATE_LIMITING";
	private final String OUTPUT_FLUSH_INTERVAL_KEY = "OUTPUT_FLUSH_INTERVAL";
	private final String RATE_LIMITING_DEFAULT_VALUE = String.valueOf(true);

	private final int OUTPUT_BUFFER_SIZE = 16777216; // 1024 * 1024 ^ 16;

	/**
	 * Every 50000 lines flush the buffers
	 */
	private final int OUTPUT_FLUSH_INTERVAL_MULTIPLIER = 1024;

	private final String OUTPUT_FLUSH_INTERVAL_DEFAULT = "300";

	private final int GZIP_BUFFER_LEVEL = 65536;

	/**
	 * 
	 * @param sourceS3bucket
	 * @param sourceS3Prefix
	 * @param searchString
	 * @param destinationS3Bucket
	 * @return
	 */
	public String parse(String sourceS3bucket, String sourceS3Prefix, String searchString, String destinationS3Bucket) {

		System.out.println("s3:///" + sourceS3bucket + "/" + sourceS3Prefix);

		var srcPath = Paths.get(URI.create("s3://" + sourceS3bucket + "/" + sourceS3Prefix));

		long startTime = System.currentTimeMillis();

		// destination path will use input file name as folder name
		URI dstPathUri = URI.create("s3://" + destinationS3Bucket + "/" + sourceS3Prefix + "/" + startTime);

		String result = String.format("Results will be saved to: " + dstPathUri.toString());

		System.out.println(result);

		// HOW MANY LINES ARE REQUIRED TO BE BUFFERED, EXPRESSED IN Multiplies of 1024,
		// before they are flushed to disk
		// for Lambda function with 3008 MB, we can set 300 for fastq data from SGX
		// Singapore
		int outputFlushInterval = OUTPUT_FLUSH_INTERVAL_MULTIPLIER
				* Integer.parseInt(System.getProperty(OUTPUT_FLUSH_INTERVAL_KEY, OUTPUT_FLUSH_INTERVAL_DEFAULT));

		boolean rateLimitProcessing = Boolean
				.valueOf(System.getProperty(RATE_LIMITING_KEY, RATE_LIMITING_DEFAULT_VALUE));

		try (ReadableByteChannel channel = FileChannel.open(srcPath, StandardOpenOption.READ)) {

			// create destination prefix in the destination bucket only when we can open
			// source

			// creates the directories, directories are prefixes in s3
			Path destinationS3Path = Files.createDirectories(Path.of(dstPathUri));

			var ephemeralStorageMainVolume = System.getProperty(EPHEMERAL_STORAGE_LOCATION_KEY, "/tmp");

			final File notMatchedResultsFile = new File(ephemeralStorageMainVolume, NON_MATCHED_FILE_NAME);
			notMatchedResultsFile.deleteOnExit();

			final File matchedResultsFile = new File(ephemeralStorageMainVolume, MATCHED_FILE_NAME);
			matchedResultsFile.deleteOnExit();

			// Construct a stream that reads bytes from the given channel.
			try (InputStream is = Channels.newInputStream(channel);
					GZIPInputStream gzipInputStream = new GZIPInputStream(is, GZIP_BUFFER_LEVEL);
					InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader, OUTPUT_BUFFER_SIZE)) {

				FileOutputStream fsOutputStreamNonMatching = new FileOutputStream(notMatchedResultsFile);
				GZIPOutputStream gzipOutputStreamNonMatching = new GZIPOutputStream(fsOutputStreamNonMatching,
						GZIP_BUFFER_LEVEL);

				StringBuilder nonMatchingLines = new StringBuilder(OUTPUT_BUFFER_SIZE);

				FileOutputStream fsOutputStreamMatching = new FileOutputStream(matchedResultsFile);
				GZIPOutputStream gzipOutputStreamMatching = new GZIPOutputStream(fsOutputStreamMatching,
						GZIP_BUFFER_LEVEL);
				StringBuilder matchedLines = new StringBuilder(OUTPUT_BUFFER_SIZE);

				int foundMatching = 0;
				int totalNumberOfLines = 0;
				String line;

				while ((line = bufferedReader.readLine()) != null) {
					totalNumberOfLines++;

					// major output is non matched
					if (!line.startsWith(searchString)) {
						nonMatchingLines.append(line).append("\n");
					} else {
						foundMatching++;
						matchedLines.append(line).append("\n");
					}

					if (totalNumberOfLines % outputFlushInterval == 0) {

						System.out.println("totalNumberOfLines currently processed: " + totalNumberOfLines);
						compressBufferedString(nonMatchingLines, gzipOutputStreamNonMatching);
						compressBufferedString(matchedLines, gzipOutputStreamMatching);
						nonMatchingLines.setLength(0);
						matchedLines.setLength(0);

						// this break is for Lambda testing
						// under full execution for PGO tuning executed on Fargate/EC2 this break is
						// removed

						if (rateLimitProcessing) {
							break;
						}
					}
				}

				logger.info("Number of lines {} matching {}", foundMatching, searchString);
				logger.info("Total number of all lines in the file is {}, ", totalNumberOfLines);

				compressBufferedString(nonMatchingLines, gzipOutputStreamNonMatching);
				compressBufferedString(matchedLines, gzipOutputStreamMatching);

				System.out.println("Number of lines " + foundMatching + " matching " + searchString);
				System.out.println("Number of totalNumberOfLines " + totalNumberOfLines);

				gzipOutputStreamNonMatching.flush();
				gzipOutputStreamMatching.flush();
				gzipOutputStreamNonMatching.close();
				gzipOutputStreamMatching.close();
			}

			long totalTime = System.currentTimeMillis() - startTime;

			logger.info("Total time processing {} ", String.valueOf(totalTime / 1000));
			System.out.println("Total time processing " + String.valueOf(totalTime / 1000));

			// create place holders for the files
			Path destinationS3PathNonMatched = destinationS3Path.resolve(NON_MATCHED_FILE_NAME);
			Files.write(destinationS3PathNonMatched, NON_MATCHED_FILE_NAME.getBytes());

			Path destinationS3PathMatched = destinationS3Path.resolve(MATCHED_FILE_NAME);
			Files.write(destinationS3PathNonMatched, MATCHED_FILE_NAME.getBytes());

			Files.copy(notMatchedResultsFile.toPath(), destinationS3PathNonMatched,
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(matchedResultsFile.toPath(), destinationS3PathMatched, StandardCopyOption.REPLACE_EXISTING);

			return result;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param strBuilder
	 * @throws IOException
	 */
	private void compressBufferedString(StringBuilder strBuilder, GZIPOutputStream gzipOutputStream)
			throws IOException {
		byte[] buffer = strBuilder.toString().getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

		byte[] bytes = new byte[OUTPUT_BUFFER_SIZE];
		int length;
		while ((length = bais.read(bytes)) >= 0) {
			System.out.println("Read " + length);
			gzipOutputStream.write(bytes, 0, length);
		}
		strBuilder.setLength(0);
	}
}
