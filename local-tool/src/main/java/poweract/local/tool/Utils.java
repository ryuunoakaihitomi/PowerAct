package poweract.local.tool;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class Utils {

    private Utils() {
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/zip/Deflater.html">Deflater</a>
     */
    public static byte[] compress(byte[] input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(input);
        compressor.finish();
        final int srcLen = input.length;
        byte[] output = new byte[srcLen];
        while (!compressor.finished()) {
            int compressedDataLength = compressor.deflate(output);
            out.write(output, 0, compressedDataLength);
        }
        byte[] result = out.toByteArray();
        final int tgzLen = result.length;
        System.out.println("compress: data compression ratio: [ " + tgzLen + " / " + srcLen + " ] = " + tgzLen * 100 / srcLen + "%");
        return result;
    }
}
