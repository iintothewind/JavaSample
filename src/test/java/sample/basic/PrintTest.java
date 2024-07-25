package sample.basic;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Scanner;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocFlavor.BYTE_ARRAY;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.junit.Test;
import org.springframework.core.io.Resource;
import sample.http.HttpUtil;

public class PrintTest {

    @Test
    @SneakyThrows
    public void testPrintService01() {
        // Define the text to print
        final Resource pdf = ResourceUtil.loadResource("classpath:label001.pdf");
        byte[] bytes = Files.readAllBytes(pdf.getFile().toPath());
        InputStream inputStream = new FileInputStream("C:\\Users\\ivar\\code\\JavaSample\\src\\test\\resources\\label001.pdf");
//        final byte[] bytes = "this is a test".getBytes();

        // Create a DocFlavor object to specify the document type
        DocFlavor flavor = INPUT_STREAM.AUTOSENSE;

        // Specify the attributes for the print job (like number of copies)
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(new Copies(1));

        // Locate available print services (printers)
//        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(flavor, null);
//        if (printServices.length == 0) {
//            System.out.println("No printers found.");
//            return;
//        }

        // Choose the first available print service (printer)
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        // Create a print job
        DocPrintJob printJob = printService.createPrintJob();

        // Create a Doc object representing the document to be printed
        Doc doc = new SimpleDoc(inputStream, flavor, null);

        try {
            // Send the print job
            printJob.print(doc, attrs);
            System.out.println("Print job sent to " + printService.getName());
        } catch (PrintException e) {
            e.printStackTrace();
        }

    }

    @Test
    @SneakyThrows
    public void testPrintService02() {
        InputStream inputStream = new FileInputStream("C:\\Users\\ivar\\code\\JavaSample\\src\\test\\resources\\label001.pdf");
        Doc pdfDoc = new SimpleDoc(inputStream, INPUT_STREAM.AUTOSENSE, null);
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        DocPrintJob printJob = printService.createPrintJob();
        printJob.print(pdfDoc, new HashPrintRequestAttributeSet());
        inputStream.close();
    }

    private static PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        return null;
    }

    @Test
    @SneakyThrows
    public void testPrintService03() {
        final File file = new File("C:\\Users\\ivar\\code\\JavaSample\\src\\test\\resources\\label001.pdf");
        PDDocument document = PDDocument.load(file);

        //takes standard printer defined by OS
        PrintService myPrintService = PrintServiceLookup.lookupDefaultPrintService();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        job.setPrintService(myPrintService);
        job.print();
    }

    @Test
    public void testGetFlavors() {

        PrintService prnSvc = null;

        /* locate a print service that can handle it */
        PrintService[] pservices =
            PrintServiceLookup.lookupPrintServices(null, null);
        if (pservices.length > 0) {
            int ii = 0;
            while (ii < pservices.length) {
                System.out.println("Named Printer found: " + pservices[ii].getName());
                if (pservices[ii].getName().endsWith("YourPrinterName")) {
                    prnSvc = pservices[ii];
                    System.out.println("Named Printer selected: " + pservices[ii].getName() + "*");
                    break;
                }
                ii++;
            }
        }

        DocFlavor[] docFalvor = prnSvc.getSupportedDocFlavors();
        for (int i = 0; i < docFalvor.length; i++) {
            System.out.println(docFalvor[i].getMimeType());
        }
    }

    @Test
    public void testScanner() {
//        HttpUtil

    }

}
