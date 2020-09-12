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
        base64Encoder(filePath);
    }

    private static void base64Encoder(String filePath) {
        try {
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filePath)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(base64), null);
            JOptionPane.showMessageDialog(null, "base64Encoder: Copied. len=" + base64.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
