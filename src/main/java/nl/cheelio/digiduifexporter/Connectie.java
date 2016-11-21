package nl.cheelio.digiduifexporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michiel on 18-11-16.
 */
public class Connectie {
    private String naam;
    private List<String> adres = new ArrayList<String>();
    private List<String> emailAddresses = new ArrayList<String>();
    private List<String> phoneNumbers = new ArrayList<String>();

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public List<String> getAdres() {
        return adres;
    }

    public void setAdres(List<String> adres) {
        this.adres = adres;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void addEmailAdres(String emailadres) {
        emailAddresses.add(emailadres);
    }

    public void addPhone(String phone) {
        phoneNumbers.add(phone);
    }

    public void addAdres(String adresLine) {
        adres.add(adresLine);
    }

    @Override
    public String toString() {
        return String.join(",", naam, String.join(";", adres), String.join(";", emailAddresses), String.join(";", phoneNumbers));
    }
}
