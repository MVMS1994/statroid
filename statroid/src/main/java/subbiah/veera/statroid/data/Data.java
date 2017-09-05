package subbiah.veera.statroid.data;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

@SuppressWarnings("SameParameterValue")
public class Data {
    private String network = "NA";
    private String cpu = "NA";
    private String ram = "NA";
    private String key = "NA";
    private String bat = "NA";

    @Override
    public String toString() {
        return "Network: " + getNetwork() + "\n" +
                "CPU: " + getCpu() + "\n" +
                "RAM: " + getRam() + "\n" +
                "Battery: " + getBat();
    }


    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram + " GB";
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = String.valueOf(cpu) + "%";
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBat() {
        return bat;
    }

    public void setBat(String bat) {
        this.bat = bat + "%";
    }
}
