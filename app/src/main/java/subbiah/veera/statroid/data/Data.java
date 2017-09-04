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

    public Data setRam(String ram) {
        this.ram = ram + " GB";
        return this;
    }

    public String getCpu() {
        return cpu;
    }

    public Data setCpu(int cpu) {
        this.cpu = String.valueOf(cpu) + "%";
        return this;
    }

    public String getNetwork() {
        return network;
    }

    public Data setNetwork(String network) {
        this.network = network;
        return this;
    }

    public String getKey() {
        return key;
    }

    public Data setKey(String key) {
        this.key = key;
        return this;
    }

    public String getBat() {
        return bat;
    }

    public Data setBat(String bat) {
        this.bat = bat + "%";
        return this;
    }
}
