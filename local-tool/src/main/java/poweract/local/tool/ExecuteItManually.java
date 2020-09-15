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
        copyFile2CompressedBase64(filePath);
    }

    private static void copyFile2CompressedBase64(String filePath) {
        try {
            String base64 = Base64.getEncoder().encodeToString(Utils.compress(Files.readAllBytes(Paths.get(filePath))));
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            defaultToolkit.getSystemClipboard().setContents(new StringSelection(base64), null);
            defaultToolkit.beep();
            JOptionPane.showMessageDialog(null, "Copied. len=" + base64.length(), "copyFile2CompressedBase64", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
