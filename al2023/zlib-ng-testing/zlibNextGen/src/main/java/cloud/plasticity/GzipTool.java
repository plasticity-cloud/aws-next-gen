package cloud.plasticity;

import java.util.zip.GZIPOutputStream;

import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class GzipTool {

    public static void main(String[] args) {
        
        String gzipFile = args[0];
        String newFile = args[1];
        
	Path source = Paths.get(gzipFile);
        Path target = Paths.get(newFile);

        try{
           decompressGzip(source,target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void decompressGzip(Path source, Path target) throws IOException {

        int inputBufferSize = 128 * 1024;
	int outputBufferSize = inputBufferSize * 8;

        try (GZIPInputStream gis = new GZIPInputStream(
				      new BufferedInputStream(
                                      new FileInputStream(source.toFile())),inputBufferSize);
            OutputStream fos = new BufferedOutputStream(
		                      new FileOutputStream( target.toFile() ),outputBufferSize)) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[outputBufferSize];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
}
