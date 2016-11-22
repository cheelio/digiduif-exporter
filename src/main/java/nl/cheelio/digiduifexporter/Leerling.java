package nl.cheelio.digiduifexporter;

import com.google.common.base.Joiner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michiel on 18-11-16.
 */
public class Leerling {
    private String naam;
    private LocalDate geboortedatum;
    private String geslacht;
    private String jaargroep;
    private String activatiecode;
    private List<Connectie> connecties = new ArrayList<Connectie>();


    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getGeslacht() {
        return geslacht;
    }

    public void setGeslacht(String geslacht) {
        this.geslacht = geslacht;
    }

    public String getJaargroep() {
        return jaargroep;
    }

    public void setJaargroep(String jaargroep) {
        this.jaargroep = jaargroep;
    }

    public String getActivatiecode() {
        return activatiecode;
    }

    public void setActivatiecode(String activatiecode) {
        this.activatiecode = activatiecode;
    }

    public List<Connectie> getConnecties() {
        return connecties;
    }

    public void setConnecties(List<Connectie> connecties) {
        this.connecties = connecties;
    }

    public void addConnectie(Connectie connectie) {
        connecties.add(connectie);
    }


    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    @Override
    public String toString() {
        return "Leerling{" +
                "naam='" + naam + '\'' +
                ", geboortedatum=" + geboortedatum +
                ", geslacht='" + geslacht + '\'' +
                ", jaargroep='" + jaargroep + '\'' +
                ", activatiecode='" + activatiecode + '\'' +
                ", connecties=" + connecties +
                '}';
    }
}
