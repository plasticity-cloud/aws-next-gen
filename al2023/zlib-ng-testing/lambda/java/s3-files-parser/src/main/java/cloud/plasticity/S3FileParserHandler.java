package cloud.plasticity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3FileParserHandler {

	// private LambdaLogger logger = context.getLogger();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	private final String NON_MATCHED_FILE_NAME="NON_MATCHED.gz";
	private final String MATCHED_FILE_NAME="MATCHED.gz";
	
	private final int OUTPUT_BUFFER_SIZE  = 1024 *  64;
	
	
	/**
	 * Every 10000 lines flush the buffers
	 */
	private final int OUTPUT_FLUSH_INTERVAL = 10000;

	public String parse(String s3bucket, String s3path, String searchString, String s3ResultsBucket) {

		// LambdaLogger logger = context.getLogger();
		//
		//
		System.out.println("s3:///" + s3bucket + "/" + s3path);

		var srcPath = Paths.get(URI.create("s3://" + s3bucket + "/" + s3path));
		
		//destination path will use input file name as folder name
		var dstPathUri = URI.create("s3://" + s3ResultsBucket + "/" + s3path);
		
		

		long startTime = System.currentTimeMillis();


		try (ReadableByteChannel channel = FileChannel.open(srcPath, StandardOpenOption.READ)) {
			
			
			//create destination prefix in the destination bucket only when we can open source
			
			// creates the directories (called a prefix in s3)
	        var dstPath = Files.createDirectories(Path.of(dstPathUri));

			// Construct a stream that reads bytes from the given channel.

	        
	        var ephemeralStorageMainVolume = "/tmp";
	        
			try (InputStream is = Channels.newInputStream(channel);
					GZIPInputStream gzipInputStream = new GZIPInputStream(is);
					InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

				
				
				FileOutputStream fsOutputStreamNonMatching = new FileOutputStream(new File(ephemeralStorageMainVolume,NON_MATCHED_FILE_NAME));
				GZIPOutputStream gzipOutputStreamNonMatching = new GZIPOutputStream(fsOutputStreamNonMatching, OUTPUT_BUFFER_SIZE);

				StringBuilder nonMatchingLines = new StringBuilder(OUTPUT_BUFFER_SIZE * 8);
				
				FileOutputStream fsOutputStreamMatching = new FileOutputStream(new File(ephemeralStorageMainVolume,MATCHED_FILE_NAME));
				GZIPOutputStream gzipOutputStreamMatching = new GZIPOutputStream(fsOutputStreamMatching, OUTPUT_BUFFER_SIZE);
				StringBuilder matchedLines = new StringBuilder(OUTPUT_BUFFER_SIZE * 2);
				
				int foundMatching = 0;
				int totalNumberOfLines = 0;
				String line;

				while ((line = bufferedReader.readLine()) != null) {
					totalNumberOfLines++;
					
					// major output is non matched
					if (!line.startsWith(searchString)) {
						nonMatchingLines.append(line).append("\n");
					}
					else {
						foundMatching++;
						matchedLines.append(line).append("\n");
					}
					
					if(totalNumberOfLines % OUTPUT_FLUSH_INTERVAL == 0 ) {
						compressBufferedString(nonMatchingLines,gzipOutputStreamNonMatching);
						compressBufferedString(matchedLines,gzipOutputStreamMatching);
					}
				}
				
				
				logger.info("Number of lines {} matching {}", foundMatching, searchString);
				logger.info("Total number of all lines in the file is {}, ", totalNumberOfLines);
				
				compressBufferedString(nonMatchingLines,gzipOutputStreamNonMatching);
				compressBufferedString(matchedLines,gzipOutputStreamMatching);

				System.out.println("Number of lines " + foundMatching  + " matching " + searchString );
				System.out.println("Number of totalNumberOfLines " + totalNumberOfLines);

				gzipOutputStreamNonMatching.flush();
				gzipOutputStreamMatching.flush();
				gzipOutputStreamNonMatching.close();
				gzipOutputStreamMatching.close();
			}

			long totalTime = System.currentTimeMillis() - startTime;

			logger.info("Total time processing {} ", String.valueOf(totalTime / 1000));
			System.out.println("Total time processing " + String.valueOf(totalTime / 1000));

			
			return String.format("Results saved in " + dstPathUri.toURL().toString() );

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * 
	 * @param strBuilder
	 * @throws IOException
	 */
	private void compressBufferedString(StringBuilder strBuilder) throws IOException {

		int bufferSize = strBuilder.length();

		ByteArrayInputStream bais = new ByteArrayInputStream(strBuilder.toString().getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);

		byte[] bytes = new byte[1024 * 64];
		int length;
		while ((length = bais.read(bytes)) >= 0) {
			gzipOutputStream.write(bytes, 0, length);
		}

		gzipOutputStream.close();
		bais.close();
		baos.close();
	}
	
	/**
	 * 
	 * @param strBuilder
	 * @throws IOException
	 */
	private void compressBufferedString(StringBuilder strBuilder, GZIPOutputStream gzipOutputStream) throws IOException {

		int bufferSize = strBuilder.length();

		ByteArrayInputStream bais = new ByteArrayInputStream(strBuilder.toString().getBytes());

		byte[] bytes = new byte[OUTPUT_BUFFER_SIZE];
		int length;
		while ((length = bais.read(bytes)) >= 0) {
			gzipOutputStream.write(bytes, 0, length);
		}
		
		strBuilder.setLength(0);
	}
}
