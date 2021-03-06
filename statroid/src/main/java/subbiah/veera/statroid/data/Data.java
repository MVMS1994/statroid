package subbiah.veera.statroid.data;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

@SuppressWarnings("SameParameterValue")
public class Data {
    private static Data _instance;
    private double network = 0;
    private double cpu = 0;
    private double ram = 0;
    private String key = "NA";
    private double bat = 0;
    private double totalRam;
    private double upload = 0;
    private double download = 0;

    private Data() {}

    public static Data init() {
        if(_instance == null) {
            _instance = new Data();
        }
        return _instance;
    }

    public static void reset() {
        _instance = null;
    }

    @Override
    public String toString() {
        return "Network: " + getNetwork() + "\n" +
                "CPU: " + getCpu() + "\n" +
                "RAM: " + getRam() + "\n" +
                "Battery: " + getBat();
    }


    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getNetwork() {
        return network;
    }

    public void setNetwork(double upload, double download) {
        this.upload = upload;
        this.download = download;
        this.network = this.upload + this.download;
    }

    public double getUpload() {
        return upload;
    }

    public double getDownload() {
        return download;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getBat() {
        return bat;
    }

    public void setBat(double bat) {
        this.bat = bat;
    }

    public void setTotalRam(double totalRam) {
        this.totalRam = totalRam;
    }

    public double getTotalRam() {
        return totalRam;
    }
}
