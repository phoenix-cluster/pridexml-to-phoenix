package ncpsb.phoenixcluster.pridexmltophoenix.model;

/**
 * Created by baimi on 2017/11/10.
 * This class stores the table data for the front end system, for the convenient of server pagination.
 */
public class ScoredPSMRow {
    Integer id;
    String peptideSequence;
    String clusterId;
    Double confidentScore;
    Double clusterRatio;
    Double clusterSize;
    String recommendPeptide;
    Integer NumSpectra;//the number of spectra which are identified as this peptide and matches with this cluster.
    String spectrumIds;//id list of the spectra which are identified as this peptide and matches with this cluster.

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPeptideSequence() {
        return peptideSequence;
    }

    public void setPeptideSequence(String peptideSequence) {
        this.peptideSequence = peptideSequence;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Double getConfidentScore() {
        return confidentScore;
    }

    public void setConfidentScore(Double confidentScore) {
        this.confidentScore = confidentScore;
    }

    public Double getClusterRatio() {
        return clusterRatio;
    }

    public void setClusterRatio(Double clusterRatio) {
        this.clusterRatio = clusterRatio;
    }

    public Double getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(Double clusterSize) {
        this.clusterSize = clusterSize;
    }

    public String getRecommendPeptide() {
        return recommendPeptide;
    }

    public void setRecommendPeptide(String recommendPeptide) {
        this.recommendPeptide = recommendPeptide;
    }

    public Integer getNumSpectra() {
        return NumSpectra;
    }

    public void setNumSpectra(Integer numSpectra) {
        NumSpectra = numSpectra;
    }

    public String getSpectrumIds() {
        return spectrumIds;
    }

    public void setSpectrumIds(String spectrumIds) {
        this.spectrumIds = spectrumIds;
    }
}
