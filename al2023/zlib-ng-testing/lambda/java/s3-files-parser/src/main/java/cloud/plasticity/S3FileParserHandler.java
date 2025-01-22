package cloud.plasticity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class S3FileParserHandler {

	public String parse(String s3bucket, String s3path, String searchString) {
		var path = Paths.get(URI.create("s3://" + s3bucket + "/" + s3path));
		
		long startTime = System.currentTimeMillis();
		
                StringBuilder strBuilder = new StringBuilder();

		try (ReadableByteChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

			// Construct a stream that reads bytes from the given channel.

			try (InputStream is = Channels.newInputStream(channel);
					GZIPInputStream gzipInputStream = new GZIPInputStream(is);
					InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
				
				int warcCounter=0;
				int totalNumberOfLines=0;
				String line;
				
				
				while ((line = bufferedReader.readLine()) != null) {
					totalNumberOfLines++;
				    if(line.startsWith(searchString)) {
				    	warcCounter++;
				    	strBuilder.append(line).append("\n");
				    }
				}
				
				System.out.println("Number of WARC-Date lines " + warcCounter);
				System.out.println(strBuilder.toString());
				System.out.println("Number of totalNumberOfLines " +  totalNumberOfLines );
				
			}

			
		long totalTime = System.currentTimeMillis() - startTime;
		
		

		System.out.print("Total time " + String.valueOf(totalTime / 1000));
		
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return strBuilder.toString();
	}

}
