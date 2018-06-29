import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class FlowerClassification {

  private static final String server_host = "47.106.157.25";
  private static final int server_port = 8432;
  private static final String INFO_HEADER = "FC";
  private static final String SERVER_RDY = "FCRDY";
  private static final String CLA_RESULT = "FCRES";

  public static int classify(String img_file) {
    try {
      Socket socket = new Socket(server_host, server_port);
      OutputStream outputStream = socket.getOutputStream();
      OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
      BufferedWriter bufWriter = new BufferedWriter(outputWriter);
      BufferedReader bufReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String suffix = img_file.substring(img_file.lastIndexOf(".") + 1);
      File img = new File(img_file);
      long file_size = img.length();
      String file_info = INFO_HEADER + "|" + file_size + "|" + suffix + "|";
      System.out.println("Sending header: " + file_info);
      bufWriter.write(file_info);
      bufWriter.flush();

      FileInputStream fileInput = new FileInputStream(img_file);
      int size = -1;
      byte[] buffer = new byte[1024];
      int sended_size = 0;

      // String response = bufReader.readLine();
      // System.out.println("Get response: " + response);
      // if (!response.equals(SERVER_RDY)) {
      // socket.close();
      // return -1;
      // }

      while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
        sended_size += size;
        outputStream.write(buffer, 0, size);
        outputStream.flush();
      }
      System.out.println("Sended size: " + sended_size);

      String res_response = bufReader.readLine();
      System.out.println("Get result: " + res_response);
      String[] tokens = res_response.split("\\|");
      int result = -1;
      if (tokens.length == 2) {
        if (!tokens[0].equals(CLA_RESULT)) {
          socket.close();
          return -1;
        }
        result = Integer.valueOf(tokens[1]);
      } else {
        socket.close();
        return -1;
      }

      fileInput.close();
      bufReader.close();
      bufWriter.close();
      outputWriter.close();
      outputStream.close();
      socket.close();

      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  public static void main(String[] args) {
    String img = "./102flowers/image_00001.jpg";

    int result = -1;
    while (result == -1)
      result = classify(img);
    System.out.println("Result: " + result);

    img = "./102flowers/cat.png";
    result = -1;
    while (result == -1)
      result = classify(img);
    System.out.println("Result: " + result);
  }
}