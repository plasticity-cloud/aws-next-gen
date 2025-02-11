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
	

	private final String NON_MATCHED_FILE_NAME="NON_MATCHED.gz";
	private final String MATCHED_FILE_NAME="MATCHED.gz";
	private final String EPHEMERAL_STORAGE_LOCATION_KEY="ephemeral-location";
	
	private final int OUTPUT_BUFFER_SIZE  = 16777216; //1024 *  1024 ^ 16;
	
	
	/**
	 * Every 50000 lines flush the buffers
	 */
	private final int OUTPUT_FLUSH_INTERVAL = 1024 * 1000;
	
	private final int GZIP_BUFFER_LEVEL=65536;

	public String parse(String s3bucket, String s3path, String searchString, String s3ResultsBucket) {

		// LambdaLogger logger = context.getLogger();
		//
		//
		System.out.println("s3:///" + s3bucket + "/" + s3path);

		var srcPath = Paths.get(URI.create("s3://" + s3bucket + "/" + s3path));
		

		long startTime = System.currentTimeMillis();
		
		//destination path will use input file name as folder name
		URI dstPathUri = URI.create("s3://" + s3ResultsBucket + "/" + s3path + "/" + startTime);
		
		String result = String.format("Results will be saved to: " + dstPathUri.toString() );
		
		System.out.println("Expected " + result);

		try (ReadableByteChannel channel = FileChannel.open(srcPath, StandardOpenOption.READ)) {
			
			// create destination prefix in the destination bucket only when we can open source
			
			// creates the directories, directories are prefixes in s3
	        Path destinationS3Path = Files.createDirectories(Path.of(dstPathUri));     
	        
	        var ephemeralStorageMainVolume = System.getProperty(EPHEMERAL_STORAGE_LOCATION_KEY, "/tmp");

	        File ephemeralStoragePath = new File(ephemeralStorageMainVolume);
	        
	        final File notMatchedResultsFile = new File(ephemeralStorageMainVolume,NON_MATCHED_FILE_NAME);
	        notMatchedResultsFile.deleteOnExit();
	        
	        final File matchedResultsFile = new File(ephemeralStorageMainVolume,MATCHED_FILE_NAME);
	        matchedResultsFile.deleteOnExit();

			// Construct a stream that reads bytes from the given channel.
	        
			try (InputStream is = Channels.newInputStream(channel);
					GZIPInputStream gzipInputStream = new GZIPInputStream(is, GZIP_BUFFER_LEVEL);
					InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader,OUTPUT_BUFFER_SIZE)) {
				
				FileOutputStream fsOutputStreamNonMatching = new FileOutputStream(notMatchedResultsFile);
				GZIPOutputStream gzipOutputStreamNonMatching = new GZIPOutputStream(fsOutputStreamNonMatching, GZIP_BUFFER_LEVEL);

				StringBuilder nonMatchingLines = new StringBuilder(OUTPUT_BUFFER_SIZE);
				
				FileOutputStream fsOutputStreamMatching = new FileOutputStream(matchedResultsFile);
				GZIPOutputStream gzipOutputStreamMatching = new GZIPOutputStream(fsOutputStreamMatching, GZIP_BUFFER_LEVEL);
				StringBuilder matchedLines = new StringBuilder(OUTPUT_BUFFER_SIZE);
				
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
						
						System.out.println("totalNumberOfLines currently processed: " + totalNumberOfLines);
						compressBufferedString(nonMatchingLines,gzipOutputStreamNonMatching);
						compressBufferedString(matchedLines,gzipOutputStreamMatching);
						nonMatchingLines.setLength(0);
						matchedLines.setLength(0);
						break;
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

			
			//create place holders for the files
			Path destinationS3PathNonMatched = destinationS3Path.resolve(NON_MATCHED_FILE_NAME);
	        Files.write(destinationS3PathNonMatched, NON_MATCHED_FILE_NAME.getBytes());
			
			Path destinationS3PathMatched = destinationS3Path.resolve(MATCHED_FILE_NAME);
			Files.write(destinationS3PathNonMatched, MATCHED_FILE_NAME.getBytes());
			
	        Files.copy(notMatchedResultsFile.toPath(), destinationS3PathNonMatched, StandardCopyOption.REPLACE_EXISTING);
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

		
		byte[] buffer = strBuilder.toString().getBytes();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		
		
		byte[] bytes = new byte[OUTPUT_BUFFER_SIZE];
		int length;
		while ((length = bais.read(bytes)) >= 0) {
			System.out.println("Read " + length);
			gzipOutputStream.write(bytes, 0, length);
		}
		
//		gzipOutputStream.write(buffer, 0, buffer.length);
		
		strBuilder.setLength(0);
	}
	
	
//	/**
//	 * Optimized method from ByteArrayInputStream
//	 * @param buf
//	 * @param out
//	 * @return
//	 * @throws IOException
//	 */
//    public synchronized long transferTo(byte[] buf, OutputStream out) throws IOException {
//		int pos = 0;
//		int count = buf.length;
//        int len = count - pos;
//        if (len > 0) {
//            int nwritten = 0;
//            while (nwritten < len) {
//                int nbyte = Integer.min(len - nwritten, OUTPUT_BUFFER_SIZE);
//                out.write(buf, pos, nbyte);
//                pos += nbyte;
//                nwritten += nbyte;
//            }
//            assert pos == count;
//        }
//        return len;
//    }
}
