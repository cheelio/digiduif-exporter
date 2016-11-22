package nl.cheelio.digiduifexporter;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michiel on 19-11-16.
 */
public class DigiduifExporter {
    public static final String COLUMN_SEPARATOR = ";";
    private static final Logger logger = LoggerFactory.getLogger(DigiduifExporter.class);
    private static final String LIST_STUDENTS_URL = "https://mijn.digiduif.nl/SchoolDomain/Students/ListStudents.aspx";
    private static final String LOGIN_URL = "https://mijn.digiduif.nl/Accounts/Login";
    private final PhantomJSDriver driver;
    private int leerlingCount;
    private int currentLeerling;

    public DigiduifExporter() {
        PhantomJsDriverManager.getInstance().setup();
        driver = new PhantomJSDriver();
    }

    public static void main(String[] args) throws IOException, ParseException {
        final HelpFormatter formatter = new HelpFormatter();
        final CommandLineParser parser = new DefaultParser();
        final Options options = new Options();
        options.addOption("u", "username", true, "The digiduif login to use for exporting.");
        options.addOption("p", "password", true, "The digiduif password to use for exporting.");
        options.addOption("e", "export-file", true, "The Ouputfile which will be written in CSV format.");

        final CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption('u') && cmd.hasOption('p') && cmd.hasOption('e')) {
            final DigiduifExporter exporter = new DigiduifExporter();
            exporter.exportLeerlingen(cmd.getOptionValue('u'), cmd.getOptionValue('p'), cmd.getOptionValue('e'));
            logger.info("Done.");
        } else {
            formatter.printHelp("java -jar digiduif-exporter.jar", options);
        }
    }

    public List<Leerling> exportLeerlingen(final String emailAddress, final String password, final String filename) throws IOException {

        login(emailAddress, password);
        driver.get(LIST_STUDENTS_URL);
        leerlingCount = getLeerlingCount();
        currentLeerling = 0;
        List<Leerling> leerlingen = exportLeerlingen();
        writeToFile(leerlingen, new File(filename));
        return leerlingen;
    }

    private void writeToFile(List<Leerling> leerlingen, File file) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(file.toPath());
        int maxConnecties = 0, maxAdres = 0, maxEmail = 0, maxPhone = 0;
        for (Leerling leerling : leerlingen) {
            if (leerling.getConnecties().size() > maxConnecties) {
                maxConnecties = leerling.getConnecties().size();
            }

            for (Connectie connectie : leerling.getConnecties()) {
                if (connectie.getAdres().size() > maxAdres) {
                    maxAdres = connectie.getAdres().size();
                }
                if (connectie.getEmailAddresses().size() > maxEmail) {
                    maxEmail = connectie.getEmailAddresses().size();
                }
                if (connectie.getPhoneNumbers().size() > maxPhone) {
                    maxPhone = connectie.getPhoneNumbers().size();
                }
            }
        }

        for (Leerling leerling : leerlingen) {
            StringBuilder sb = new StringBuilder();
            sb.append(leerling.getNaam() + COLUMN_SEPARATOR);
            sb.append(leerling.getGeboortedatum() + COLUMN_SEPARATOR);
            sb.append(leerling.getGeslacht() + COLUMN_SEPARATOR);
            sb.append(leerling.getJaargroep() + COLUMN_SEPARATOR);
            for (int c = 1; c <= maxConnecties; c++) {

                if (c <= leerling.getConnecties().size()) {
                    sb.append(leerling.getConnecties().get(c).getNaam());
                }
                sb.append(COLUMN_SEPARATOR);


                for (int p = 1; p <= maxPhone; p++) {
                    if (c <= leerling.getConnecties().size() && p <= leerling.getConnecties().get(c).getPhoneNumbers().size()) {
                        sb.append(leerling.getConnecties().get(c).getPhoneNumbers().get(p));
                    }
                    sb.append(COLUMN_SEPARATOR);
                }

                for (int a = 1; a <= maxAdres; a++) {
                    if (c <= leerling.getConnecties().size() && a <= leerling.getConnecties().get(c).getAdres().size()) {
                        sb.append(leerling.getConnecties().get(c).getAdres().get(a));
                    }
                    sb.append(COLUMN_SEPARATOR);
                }

                for (int e = 1; e <= maxEmail; e++) {
                    if (c <= leerling.getConnecties().size() && e <= leerling.getConnecties().get(c).getEmailAddresses().size()) {
                        sb.append(leerling.getConnecties().get(c).getEmailAddresses().get(e));
                    }
                    sb.append(COLUMN_SEPARATOR);
                }
            }
            writer.write(sb.toString());
        }
        writer.flush();
        writer.close();
    }

    private int getLeerlingCount() {
        try {
            String pageSource = driver.getPageSource().substring(driver.getPageSource().indexOf("<div class=\"infos \">"));
            pageSource = pageSource.replaceAll("<div class=\"infos \">\n                Toont 1 t/m 15 van de (\\d*?) resultaten.*?", "$1");
            pageSource = pageSource.substring(0, pageSource.indexOf("\n"));
            return Integer.parseInt(pageSource);
        } catch (Exception e) {
            return 0;
        }
    }

    private List<Leerling> exportLeerlingen() throws IOException {
        List<Leerling> leerlingList = new ArrayList<Leerling>();
        List<WebElement> students = getStudents();
        for (int i = 0; i < students.size(); i++) {
            final WebElement studentRow = students.get(i);
            if (studentRow.findElements(By.tagName("td")).size() > 1) {
                currentLeerling++;
                long percentage = Math.round((currentLeerling / new Integer(leerlingCount).doubleValue()) * 100);
                final WebElement achternaamLink = studentRow.findElements(By.tagName("td")).get(1).findElement(By.tagName("a"));
                final WebElement voornaamLink = studentRow.findElements(By.tagName("td")).get(2).findElement(By.tagName("a"));
                logger.info("Got link for {} {}, {}/{},  {}%", voornaamLink.getText(), achternaamLink.getText(), currentLeerling, leerlingCount, percentage);
                leerlingList.add(createLeerlingFromDetailPage(achternaamLink));
                students = getStudents();
            } else {
                final List<WebElement> navLinks = studentRow.findElements(By.tagName("a"));
                final int currentPage = getLinkNumber(studentRow.findElement(By.cssSelector("a[activepage='true']")));

                for (final WebElement navLink : navLinks) {
                    final int newPage = getLinkNumber(navLink);
                    if (newPage > currentPage) {
                        if (newPage == Integer.MAX_VALUE && studentRow.findElements(By.id("lnkNextPages")).isEmpty()) {
                            return null;
                        } else {
                            navLink.click();
                            leerlingList.addAll(exportLeerlingen());
                        }
                    }
                }
            }
        }
        return leerlingList;
    }

    private List<WebElement> getStudents() {
        List<WebElement> students = driver.findElement(By.id("gvStudentsGrid")).findElements(By.tagName("tr"));
        students = students.subList(1, students.size());
        return students;
    }

    public Leerling createLeerlingFromDetailPage(final WebElement detailLink) throws IOException {
        detailLink.click();
        final Leerling leerling = new Leerling();
        final WebElement leerlingBlock = driver.findElement(By.xpath("//*[@id=\"content\"]/div/div[2]/div[1]/div[1]/div[2]/div[3]/div"));
        final String naamGeboortedatum = leerlingBlock.findElement(By.xpath("span[1]")).getText();
        final String geslacht = leerlingBlock.findElement(By.xpath("span[2]")).getText();
        final String jaargroep = leerlingBlock.findElement(By.xpath("span[3]/a")).getText();
        final String activatiecode = leerlingBlock.findElement(By.xpath("span[5]")).getText();
        leerling.setNaam(naamGeboortedatum.split("\\(")[0].trim());
        leerling.setGeboortedatum(LocalDate.parse(naamGeboortedatum.split("\\(")[1].substring(0, naamGeboortedatum.split("\\(")[1].length() - 1), DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        leerling.setGeslacht(geslacht);
        leerling.setJaargroep(jaargroep.replaceAll("Jaargroep: ", ""));
        leerling.setActivatiecode(activatiecode.replaceAll("Activatiecode ", ""));
        final List<WebElement> pmbBlocks = driver.findElementsByCssSelector("div[class='pmb-block']");

        for (final WebElement pmbBlock : pmbBlocks) {
            final String naamEnRol = pmbBlock.findElement(By.xpath("div[1]/h2")).getText();
            final WebElement connectionDetails = pmbBlock.findElement(By.xpath("div[3]/div/div[1]/div"));
            final String adres = connectionDetails.findElement(By.xpath("dl[1]/dd")).getText();
            final String emailAddresses = connectionDetails.findElement(By.xpath("dl[2]/dd")).getText();
            final String phones = connectionDetails.findElement(By.xpath("dl[3]/dd")).getText();
            final Connectie connectie = new Connectie();
            connectie.setNaam(naamEnRol.split("\n")[0]);

            for (String adresLine : adres.split("\n")) {
                if (!adres.isEmpty()) {
                    adresLine = adresLine.replaceAll("(\\d\\d\\d\\d)-([a-zA-Z][a-zA-Z])", "$1 $2");
                    connectie.addAdres(adresLine.trim());
                }
            }
            for (final String email : emailAddresses.split("\n")) {
                if (!email.isEmpty() && !email.equals("E-mailadres")) {
                    connectie.addEmailAdres(email.trim());
                }
            }
            for (final String phone : phones.split("\n")) {
                if (!phone.isEmpty() && !phone.equals("Telefoon")) {
                    connectie.addPhone(phone.trim());
                }
            }

            leerling.addConnectie(connectie);
        }
        driver.navigate().back();
        return leerling;
    }


    private void login(final String emailAddress, final String password) {
        driver.get(LOGIN_URL);
        driver.findElement(By.name("EmailAddress")).sendKeys(emailAddress);
        driver.findElement(By.name("Password")).sendKeys(password);
        driver.findElement(By.tagName("button")).click();
        logger.info("Logged in...");
    }

    private int getLinkNumber(final WebElement navLink) {
        try {
            return Integer.parseInt(navLink.getText());
        } catch (Exception e) {
            try {
                if (navLink.getAttribute("id").equals("lnkPreviousPages")) {
                    return Integer.MIN_VALUE;
                }
                if (navLink.getAttribute("id").equals("lnkNextPages")) {
                    return Integer.MAX_VALUE;
                }
            } catch (Exception e1) {
            }
            return 0;
        }
    }
}
