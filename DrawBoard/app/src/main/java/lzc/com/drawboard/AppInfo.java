package lzc.com.drawboard;

/**
 * Created by lzc on 2017/5/18.
 */

public class AppInfo {
    private int versionCode;
    private String versionName;
    private String instruction;

    public AppInfo(int versionCode,String versionName,String instruction){
        super();
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.instruction = instruction;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
