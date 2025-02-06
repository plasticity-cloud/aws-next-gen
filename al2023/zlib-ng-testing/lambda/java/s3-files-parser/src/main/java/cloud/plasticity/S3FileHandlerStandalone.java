package cloud.plasticity;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class to execute Lambda code as standalone, on e.g. Fargate or EC2
 */
public class S3FileHandlerStandalone{

	/**
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(S3FileHandlerStandalone.class);

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		/**
		 * 
		 */
		if(args.length != 4) {
			System.out.println("Please provide, 1) source S3 bucket \n, 2) S3 Bucket Prefix 3) Search keyword, 4) destination S3 bucket.");
			System.exit(0);
		}

		System.out.println(args[0] + " " + args[1] + " "  + args[2] +  " " + args[3]);
                S3FileParserHandler s3ParserHandler = new S3FileParserHandler();
                s3ParserHandler.parse(args[0], args[1], args[2], args[3]);
        }
}
