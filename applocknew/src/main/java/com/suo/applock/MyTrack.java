package com.suo.applock;


import com.android.client.AndroidSdk;

/**
 * Created by huale on 2014/12/17.
 */
public class MyTrack {
    public static final String CATE_DEFAULT = "杂项";
    public static final String ACT_UNLOCK = "解锁手机应用次数";
    public static final String ACT_APPLOCK = "成功设置密码";
    public static final String ACT_EMAIL = "保存密保邮箱";
    public static final String ACT_DLY_UNLOCK = "礼包按钮";
    public static final String CATE_MENU = "菜单";
    public static final String ACT_UNINSTALL = "卸载按钮";
    public static final String ACT_DLY_MENU = "应用推荐";
    public static final String ACT_APPLOCK_MENU = "锁应用";
    public static final String ACT_PHOTO_MENU = "锁图片";
    public static final String ACT_THEME_MENU = "主题";
    public static final String ACT_VIDEO_MENU = "锁视频";
    public static final String ACT_FILE_MENU = "锁文件";
    public static final String ACT_HIDE_MENU = "隐藏应用";
    public static final String ACT_SETTING_MENU = "设置";
    public static final String ACT_ABOUT_MENU = "关于我们";
    public static final String ACT_FAQ_MENU = "FAQ";
    public static final String CATE_ABOUT = "关于";
    public static final String ACT_SUPPORT_EMAIL = "邮箱链接";
    public static final String ACT_WEBSITE = "官网";
    public static final String ACT_GOOGLE_PLUS = "google+";
    public static final String ACT_GOOGLE_DOC = "googleDoc";
    public static final String ACT_FACEBOOK = "facebook";
    public static final String ACT_TWITTER = "twitter";
    public static final String CATE_SETTING = "设置";
    public static final String ACT_TOGGLE = "加锁保护快捷开关";
    public static final String ACT_STOP_PROTECT = "停止保护";
    public static final String ACT_BRIEF = "允许短暂退出";
    public static final String ACT_SCREEN_OFF = "锁屏重新加锁";
    public static final String ACT_ADVANCE = "高级保护";
    public static final String LABEL_ADVANCE = "激活";
    public static final String ACT_RANDOM = "随机键盘";
    public static final String CHARGING_SCREEN = "充电屏保";

    public static final String ACT_NEW_APP = "新安装提示加锁";
    public static final String ACT_HIDE_PATH = "隐藏解锁路径";
    public static final String ACT_PATTERN = "图片解锁";
    public static final String ACT_PASSWD = "文字键盘解锁";
    public static final String ACT_RATE = "评分";
    public static final String ACT_SHARE = "分享";
    public static final String CATE_RESET = "忘记密码";
    public static final String ACT_SEND = "发送验证码";
    public static final String ACT_CONFIRM = "验证验证码";
    public static final String CATE_ACTION = "使用习惯";
    public static final String ACT_LOCK_APP = "锁定APP";
    public static final String ACT_DAILY_USE = "每日使用至少1次";
    public static final String ACT_THEME_DOWNLOAD = "下载主题";
    public static final String ACT_THEME_APPLY = "使用主题";
    public static final String ACT_THEME_CUSTOM = "自定义主题";
    public static final String ACT_LOCK_PHOTO = "图片加密点击";
    public static final String ACT_LOCK_VIDEO = "视频加密点击";
    public static final String ACT_LOCK_FILE = "文件加密点击";
    public static final String LABEL_OP_FOLDER = "对目录进行操作";
    public static final String LABEL_OP_FILE = "对文件进行操作";
    public static final String ACT_UNLOCK_PHOTO = "图片解密点击";
    public static final String ACT_UNLOCK_VIDEO = "视频解密点击";
    public static final String ACT_UNLOCK_FILE = "文件解密点击";
    public static final String ACT_DEL_PHOTO = "删除图片";
    public static final String ACT_DEL_VIDEO = "删除视频";
    public static final String ACT_DEL_FILE = "删除文件";
    public static final String CATE_OVERFLOW_MENU = "溢出菜单";
    public static final String ACT_OVERFLOW_MENU_BRIEF = "短暂退出";
    public static final String ACT_OVERFLOW_MENU_UNLOCK_ME = "不再锁定";
    public static final String ACT_OVERFLOW_MENU_FORGET_PASSWD = "忘记密码";
    public static final String ACT_OVERFLOW_MENU_THEME = "主题";
    public static final String CATE_EXCEPTION = "报错";
    public static final String ACT_CRASH = "崩溃";
    public static final String CATE_HELP = "帮助";
    public static final String ACT_DAILY_APP = "每日推荐";
    public static final String ACT_PROFILE = "情景模式";
    public static final String ACT_PLUGIN = "插件";
    public static final String ACT_FAKE = "伪装";


    public static final String CATEGORY_INTRUDE = "入侵者界面";
    public static final String INTRUDE_NEXT = "下一次";
    public static final String INTRUDE_FIVE_RATE = "五星好评";


    public static final String CATEGORY_FAKECOVER= "应用伪装界面";
    public static final String FAKECOVER_NEXT= "下一次";
    public static final String FAKECOVR_FIVE_RATE = "五星好评";

    public static final String CATEGORY_VIDEO= "视频界面";
    public static final String VIDEO_NEXT = "下一次";
    public static final String VIDEO_FIVE_RATE = "五星好评";

    public static final String CATEGORY_PHOTOS= "图片界面";
    public static final String PHOTO_NEXT= "下一次";
    public static final String PHOTO_FIVE_RATE = "五星好评";


    public static final String CATEGORY_APPS= "图片界面";
    public static final String APPS_NEXT= "下一次";
    public static final String APPS_FIVE_RATE = "五星好评";

    public static final String CATEGORY_RATE_BAD_CONTENT= "差评内容";

    public static final String CATEGORY_THEMES_PAGE= "主题界面展示";








    public static void sendEvent(String cateException, String actCrash, String s, long l) {
        AndroidSdk.track(cateException, actCrash, s, (int) l);
    }
}
