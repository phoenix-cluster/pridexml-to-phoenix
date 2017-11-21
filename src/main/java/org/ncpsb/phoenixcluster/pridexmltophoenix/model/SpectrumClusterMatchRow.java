package org.ncpsb.phoenixcluster.pridexmltophoenix.model;

/**
 * Created by baimi on 2017/11/10.
 * This class stores the matches between SpectrumCluster got by spectra library search.
 */
public class SpectrumClusterMatchRow {
    String spectrumTitle;
    String clusterId;
    Double dotValue;
    Double fval;
    Double confidentScore;

    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Double getDotValue() {
        return dotValue;
    }

    public void setDotValue(Double dotValue) {
        this.dotValue = dotValue;
    }

    public Double getFval() {
        return fval;
    }

    public void setFval(Double fval) {
        this.fval = fval;
    }

    public Double getConfidentScore() {
        return confidentScore;
    }

    public void setConfidentScore(Double confidentScore) {
        this.confidentScore = confidentScore;
    }
}
