package sample.basic;

import java.util.Scanner;

public class ScannerTest {

    public static void main(String[] args) {
        while (true) {
            Scanner sc = new Scanner(System.console().reader());
            final String line = sc.nextLine();
            System.out.println(line);
        }
    }

}
