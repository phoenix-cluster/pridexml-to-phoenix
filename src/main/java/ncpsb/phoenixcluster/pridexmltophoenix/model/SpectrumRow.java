package ncpsb.phoenixcluster.pridexmltophoenix.model;

/**
 * Created by baimi on 2017/11/10.
 * This class stores the identified and unidentified spectra from PRIDE XML file
 */
public class SpectrumRow {
    String spectrumTitle; //primary key
    Double precursorMz;
    Double precursorIntens;
    Integer charge;
    String peaklistMz;
    String peaklistIntens;

    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }

    public Double getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(Double precursorMz) {
        this.precursorMz = precursorMz;
    }

    public Double getPrecursorIntens() {
        return precursorIntens;
    }

    public void setPrecursorIntens(Double precursorIntens) {
        this.precursorIntens = precursorIntens;
    }

    public Integer getCharge() {
        return charge;
    }

    public void setCharge(Integer charge) {
        this.charge = charge;
    }

    public String getPeaklistMz() {
        return peaklistMz;
    }

    public void setPeaklistMz(String peaklistMz) {
        this.peaklistMz = peaklistMz;
    }

    public String getPeaklistIntens() {
        return peaklistIntens;
    }

    public void setPeaklistIntens(String peaklistIntens) {
        this.peaklistIntens = peaklistIntens;
    }
}
