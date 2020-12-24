import calculator.MetricsCalculator;

import java.io.BufferedReader;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        String line;
        MetricsCalculator.start("C:\\Users\\temp\\Downloads\\jcommander-main");
        BufferedReader br = new BufferedReader(MetricsCalculator.printResults());

        while ((line = br.readLine()) != null)
            System.out.println(line);
    }
}
