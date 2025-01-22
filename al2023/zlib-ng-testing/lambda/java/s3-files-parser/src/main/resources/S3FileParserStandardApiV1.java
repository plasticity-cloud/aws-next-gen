//
//
//import java.net.URI;
//import java.nio.ByteBuffer;
//import java.nio.channels.AsynchronousFileChannel;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.concurrent.TimeUnit;
//
//public class S3FileParserStandardApiV1 {
//	
//	
//	/**
//	 * 
//	 * commoncrawl
//	 * crawl-data/CC-MAIN-2024-51/segments/1733066035857.0/wet/CC-MAIN-20241201162023-20241201192023-00000.warc.wet.gz
//	 * @param args
//	 */
//	public static void main(String args[]) {
//		
//		S3FileParserHandler s3ParserHandler = new S3FileParserHandler();
//		s3ParserHandler.parse(args[0], args[1]);
//	}
//	
//	public void parse(String s3bucket, String s3path) {
//		System.out.format("Downloading %s from S3 bucket %s...\n", s3path, s3bucket);
//		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
//		try {
//		    S3Object o = s3.getObject(s3bucket, s3path);
//		    S3ObjectInputStream s3is = o.getObjectContent();
//		    FileOutputStream fos = new FileOutputStream(new File(key_name));
//		    byte[] read_buf = new byte[1024];
//		    int read_len = 0;
//		    while ((read_len = s3is.read(read_buf)) > 0) {
//		        fos.write(read_buf, 0, read_len);
//		    }
//		    s3is.close();
//		    fos.close();
//		} catch (AmazonServiceException e) {
//		    System.err.println(e.getErrorMessage());
//		    System.exit(1);
//		} catch (FileNotFoundException e) {
//		    System.err.println(e.getMessage());
//		    System.exit(1);
//		} catch (IOException e) {
//		    System.err.println(e.getMessage());
//		    System.exit(1);
//		}
//	}
//	
//	
//}
package cloud;


