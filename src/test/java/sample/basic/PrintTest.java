package sample.basic;

import com.google.common.collect.ImmutableList;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@Slf4j
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

    public static PrintService findPrintService(final String printerName) {
        final PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        final PrintService printService = Vector.of(printServices)
            .find(s -> Optional
                .ofNullable(s)
                .map(PrintService::getName)
                .filter(n -> n.toLowerCase().contains(printerName.toLowerCase()))
                .isPresent())
            .getOrNull();
        if (Objects.isNull(printService)) {
            log.error("unable to find printer: {}", printerName);
        }

        return printService;
    }

    public static byte[] convertToA4(final byte[] content) {
        try (final PDDocument doc = PDDocument.load(content);
//        try (final PDDocument doc = PDDocument.load(content);
            final PDDocument newDoc = new PDDocument()) {
            if (Objects.nonNull(doc)) {
                Optional.ofNullable(doc)
                    .map(PDDocument::getPages)
                    .map(ImmutableList::copyOf)
                    .orElse(ImmutableList.of())
                    .stream()
                    .map(page -> {
                        final PDPage newPage = new PDPage(PDRectangle.A4);
                        newPage.setContents(Try.of(page::getContentStreams).filter(Objects::nonNull).map(ImmutableList::copyOf).getOrNull());
                        return newPage;
                    })
                    .forEach(newDoc::addPage);
                final File tempFile = Files.createTempFile("ulala", ".pdf").toFile();
                newDoc.save(tempFile);
                final byte[] bytes = Files.readAllBytes(tempFile.toPath());
                return bytes;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new byte[0];
    }

    @Test
    @SneakyThrows
    public void testPrintService03() {
        final File file = new File("C:\\Users\\ivar\\code\\JavaSample\\src\\test\\resources\\label001.pdf");
        PDDocument document = PDDocument.load(Files.readAllBytes(file.toPath()));

//        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
//        attr.add(MediaSizeName.ISO_A4);
//        attr.add(OrientationRequested.PORTRAIT);
//        attr.add(new MediaPrintableArea(0f, 0f, MediaSize.ISO.A4.getX(MediaSize.INCH), MediaSize.ISO.A4.getY(MediaSize.INCH), MediaSize.INCH));
//        attr.add(new PageRanges(1, document.getNumberOfPages()));


        // Define 4x6 inches paper size
        float widthInches = 4f;
        float heightInches = 6f;
        float marginInches = 0f;

        // Convert inches to points (1 inch = 72 points)
        float widthPoints = widthInches * 72;
        float heightPoints = heightInches * 72;

        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(new MediaPrintableArea(marginInches, marginInches, widthInches, heightInches, MediaSize.INCH));
        attr.add(OrientationRequested.PORTRAIT); // Typically portrait for shipping labels
        attr.add(new PageRanges(1, document.getNumberOfPages()));

        //takes standard printer defined by OS
        PrintService myPrintService = findPrintService("7900");
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new PDFPrintable(document, Scaling.SHRINK_TO_FIT));
        job.setPrintService(myPrintService);
        job.print(attr);
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
