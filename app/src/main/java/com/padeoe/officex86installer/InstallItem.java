package com.padeoe.officex86installer;

/**
 * Created by padeoe on 2015/12/6.
 */
public class InstallItem {
    private int obbsize;
    private int obbID;
    private int apkID;
    private String apkName;
    private String obbName;
    private String packageFolderName;
    private String versionName;
    private int buttonId;
    private int versionTextViewId;

    public InstallItem(int obbsize, int obbID, int apkID, String apkName, String obbName,String packageFolderName,String versionName,int buttonId,int versionTextViewId) {
        this.obbsize = obbsize;
        this.obbID = obbID;
        this.apkID = apkID;
        this.apkName = apkName;
        this.obbName = obbName;
        this.packageFolderName=packageFolderName;
        this.versionName=versionName;
        this.buttonId=buttonId;
        this.versionTextViewId=versionTextViewId;
    }

    public int getObbsize() {
        return obbsize;
    }

    public int getObbID() {
        return obbID;
    }

    public int getApkID() {
        return apkID;
    }

    public String getApkName() {
        return apkName;
    }

    public String getObbName() {
        return obbName;
    }

    public String getPackageFolderName() {
        return packageFolderName;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getButtonId() {
        return buttonId;
    }

    public int getVersionTextViewId() {
        return versionTextViewId;
    }
}
