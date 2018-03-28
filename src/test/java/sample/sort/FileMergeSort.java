package sample.sort;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class FileMergeSort {
  private static final Logger log = LogManager.getLogger();
  private static final long total = 999999L;
  private static final int limit = 9999;

  private static void cleanTempFiles() {
    FilenameFilter filter = new AndFileFilter(ImmutableList.of(new PrefixFileFilter("sort"), new SuffixFileFilter(".part")));
    ImmutableList.copyOf(FileUtils.getTempDirectory().listFiles(filter)).forEach(File::delete);
  }

  private static long lineNumber(File input) throws IOException {
    try (Stream<String> stream = Files.newReader(input, Charsets.UTF_8).lines()) {
      return stream.count();
    }
  }

  private static File split(File input, Long from, Long to) throws IOException {
    File part = File.createTempFile("sort", ".part");
    Long lineNumber = 0L;
    String line = null;
    try (Stream<String> stream = Files.newReader(input, Charsets.UTF_8).lines();
         Writer writer = Files.newWriter(part, Charsets.UTF_8)) {
      Iterator<String> lineIterator = stream.iterator();
      while (lineIterator.hasNext() && lineNumber <= to) {
        line = lineIterator.next();
        if (lineNumber >= from) {
          writer.write(line.concat(IOUtils.LINE_SEPARATOR));
        }
        lineNumber++;
      }
    }
    return part;
  }

  private static File merge(File source, File left, File right) throws IOException {
    try (Stream<String> leftStream = Files.newReader(left, Charsets.UTF_8).lines();
         Stream<String> rightStream = Files.newReader(right, Charsets.UTF_8).lines();
         Writer writer = Files.newWriter(source, Charsets.UTF_8)) {
      PeekingIterator<String> leftPeekingIterator = Iterators.peekingIterator(leftStream.iterator());
      PeekingIterator<String> rightPeekingIterator = Iterators.peekingIterator(rightStream.iterator());
      String leftLine, rightLine;
      writer.write("");
      while (leftPeekingIterator.hasNext() && rightPeekingIterator.hasNext()) {
        leftLine = leftPeekingIterator.peek();
        rightLine = rightPeekingIterator.peek();
        if (leftLine.compareTo(rightLine) < 0) {
          writer.append(leftLine.concat(IOUtils.LINE_SEPARATOR));
          leftPeekingIterator.next();
        } else {
          writer.append(rightLine.concat(IOUtils.LINE_SEPARATOR));
          rightPeekingIterator.next();
        }
      }
      while (leftPeekingIterator.hasNext()) {
        writer.append(leftPeekingIterator.next().concat(IOUtils.LINE_SEPARATOR));
      }
      while (rightPeekingIterator.hasNext()) {
        writer.append(rightPeekingIterator.next().concat(IOUtils.LINE_SEPARATOR));
      }
    }
    return source;
  }

  private static File directSort(File input) throws IOException {
    List<String> list = new ArrayList<>(limit);
    try (Stream<String> stream = Files.newReader(input, Charsets.UTF_8).lines()) {
      stream.forEach(list::add);
    }
    list.sort(String::compareTo);
    FileUtils.writeLines(input, list);
    return input;
  }

  public static File mergeSort(File input) throws IOException {
    long total = lineNumber(input);
    if (total <= limit) {
      return directSort(input);
    }
    long half = total / 2;
    File left = mergeSort(split(input, 0L, half));
    File right = mergeSort(split(input, half + 1, total));
    return merge(input, left, right);
  }


  @Ignore
  @BeforeClass
  public static void init() throws IOException {
    cleanTempFiles();
    int minLength = String.valueOf(total).length();
    try (Writer writer = Files.newWriter(new File("long.txt"), Charsets.UTF_8)) {
      writer.write("");
      for (long i = total; i > 0L; i--) {
        writer.append(Strings.padStart(String.valueOf(i), minLength, '0').concat(IOUtils.LINE_SEPARATOR));
      }
    }
  }

  @Ignore
  @BeforeClass
  @AfterClass
  public static void clean() {
    cleanTempFiles();
    new File("long.txt").delete();
  }

  @Ignore
  @Test
  public void testSort() throws IOException {
    Stopwatch watch = Stopwatch.createStarted();
    File sorted = mergeSort(new File("long.txt"));
    watch.stop();
    log.info(String.format("cost: %s MILLISECONDS", watch.elapsed(TimeUnit.MILLISECONDS)));
    log.info(String.format("cost: %s MICROSECONDS per line", watch.elapsed(TimeUnit.MICROSECONDS) / total));
  }

}
