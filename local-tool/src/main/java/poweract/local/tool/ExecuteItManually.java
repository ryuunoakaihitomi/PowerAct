package poweract.local.tool;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.swing.JOptionPane;

public class ExecuteItManually {

    public static void main(String[] args) {
        String filePath = "***";
        file2CompressedBase64(filePath);
    }

    private static void file2CompressedBase64(String filePath) {
        try {
            String base64 = Base64.getEncoder().encodeToString(Utils.compress(Files.readAllBytes(Paths.get(filePath))));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(base64), null);
            JOptionPane.showMessageDialog(null, "base64Encoder: Copied. len=" + base64.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
