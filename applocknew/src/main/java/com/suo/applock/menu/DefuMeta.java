package com.suo.applock.menu;

/**
 * Created by SongHualin on 5/5/2015.
 */
public class DefuMeta {
    public String name;
    public String desc;
    public String url;
    public String icon;
    public String pkg;
    public boolean hide;
    public boolean installed = false;
    private static final String delim = "\\@\\#\\$";
    private static final String delim_append = "@#$";
    public static final String PLUGIN_PREFIX = "lockscreen_plugin.";
    public static final String PLUGIN_ICON_PREFIX = "lockscreen_plugin.suo_ic.";

    public DefuMeta() {
    }

    public DefuMeta(String name, String desc, String url, String pkg, String icon, boolean hide) {
        this.name = name;
        this.desc = desc;
        this.url = url;
        this.icon = icon;
        this.pkg = pkg;
        this.hide = hide;
    }

    public static DefuMeta fromString(String string) {
        String[] fields = string.split(delim);
        DefuMeta pd = new DefuMeta();
        pd.name = fields[0];
        pd.desc = fields[1];
        pd.url = fields[2];
        pd.icon = fields[3];
        pd.pkg = fields[4];
        pd.hide = Boolean.parseBoolean(fields[5]);
        return pd;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(delim_append)
                .append(desc).append(delim_append)
                .append(url).append(delim_append)
                .append(icon).append(delim_append)
                .append(pkg).append(delim_append).append(hide);
        return sb.toString();
    }
}
