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


    public String getRam() {
        return ram;
    }

    public Data setRam(double ram) {
        this.ram = String.valueOf(ram) + " GB";
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

    @Override
    public String toString() {
        return "Network: " + getNetwork() + "  " +
               "CPU: " + getCpu() + "\n" +
               "RAM: " + getRam();
    }

}
