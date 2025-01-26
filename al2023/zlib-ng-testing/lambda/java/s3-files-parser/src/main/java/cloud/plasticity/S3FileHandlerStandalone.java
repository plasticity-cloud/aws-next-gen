package cloud.plasticity;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3FileHandlerStandalone{
 

	private static final Logger logger = LoggerFactory.getLogger(S3FileHandlerStandalone.class);

	public static void main(String args[]) {


		System.out.println(args[0] + " " + args[1] + " "  + args[2]);
                S3FileParserHandler s3ParserHandler = new S3FileParserHandler();
                s3ParserHandler.parse(args[0], args[1], args[2]);
        }
}
