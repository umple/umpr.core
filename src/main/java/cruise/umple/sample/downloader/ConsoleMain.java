package cruise.umple.sample.downloader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cruise.umple.sample.downloader.util.Triple;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cruise.umple.compiler.EcoreImportHandler;
import cruise.umple.compiler.UmpleImportModel;

public class ConsoleMain {

  private final Logger logger;
  private final DocumentFactory documentFactory;

  /**
   * Creates a new console main instance.
   *
   * @param logger
   * @param docFactory
   */
  @Inject
  ConsoleMain(Logger logger, DocumentFactory docFactory) {
    this.logger = logger;
    this.documentFactory = docFactory;
  }

  public void run() {
    Document doc = documentFactory.fromURL("http://www.emn.fr/z-info/atlanmod/index.php/Ecore");
    Elements top = doc.select("div#bodyContent p + ul");
    File dir = new File("/tmp/output");
    dir.mkdir();
//		FileUtils.copyURLToFile(source, destination);
//

    System.out.printf("Downloading files ->\n");
    top.stream().parallel()
            .map(e -> e.select("li a"))
            .flatMap(e -> e.stream()).map(e -> {
      try {
        return new URL(e.attr("href"));
      } catch (MalformedURLException mue) {
        throw new IllegalArgumentException(mue);
      }
    })
            .limit(50) // TODO Remove me later
            .map(url -> {
              Path path = Paths.get(url.getPath());
              Path writePath = Paths.get(dir.getAbsolutePath(), path.getFileName().toString());

              try (FileOutputStream fos = new FileOutputStream(writePath.toString())) {
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
              } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
              }

              return writePath;
            })
            .map(ConsoleMain::loadECore)
            .forEach(t -> {
              Path xmi = t.first;
              t.second.ifPresent(umple -> {
                try (PrintWriter pw = new PrintWriter(new File(xmi.toString() + ".ump"))) {
                  pw.write(t.second.get());
                } catch (IOException ioe) {
                  throw new IllegalStateException(ioe);
                }
              });

              t.third.ifPresent(e -> {
                System.err.printf("Failed to parse %s, reason: %s", xmi, e.getMessage());
              });
            });

    System.out.println("Saved Ecore files from: " + new File("lib/AtlanMod_ecore.html").toString());

  }

  
  private static Triple<Path, Optional<String>, Optional<Exception>> loadECore(Path xmi) {
    EcoreImportHandler handler = new EcoreImportHandler();
    UmpleImportModel model;
    try {
      model = handler.readDataFromXML(xmi.toAbsolutePath().toString());
      String output = model.generateUmple();
      if (!"".equals(output)) {
        return new Triple<>(xmi, Optional.of(output), Optional.empty());
      } else {
        return new Triple<>(xmi, Optional.empty(), Optional.of(new IllegalStateException("Failed to import " + xmi)));
      }
    } catch (IOException | ParserConfigurationException e) {
      return new Triple<>(xmi, Optional.empty(), Optional.of(e));
    }
  }
	
  public static void main(String[] args) throws IOException {
    Injector in = Guice.createInjector(new DownloaderModule());

    ConsoleMain main = in.getInstance(ConsoleMain.class);

    // actual page: http://www.emn.fr/z-info/atlanmod/index.php/Ecore
//		Document doc = Jsoup.parse(new File("lib/AtlanMod_ecore.html"), "utf-8");
  }
}
