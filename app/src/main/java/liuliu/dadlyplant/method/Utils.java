package liuliu.dadlyplant.method;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liuliu.dadlyplant.R;
import liuliu.dadlyplant.base.BaseApplication;

/**
 * Created by Administrator on 2016/5/19.
 */
public class Utils {

    public static Dialog setDialog(String cont, final setSure sure, final setCancle cancle) {
        return setDialog("提示", cont, "确定", "取消", sure, cancle);
    }

    public static Dialog setDialog(String title, String cont, String sure_str, String cancle_str, final setSure sure, final setCancle cancle) {
        AlertDialog.Builder localBuilder1 = new AlertDialog.Builder(BaseApplication.getContext())
                .setTitle(title).setMessage(cont);
        return localBuilder1.setPositiveButton(cancle_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancle.click();
            }
        })
                .setNegativeButton(sure_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface,
                                        int paramInt) {
                        sure.click();
                    }
                }).create();
    }

    public interface setSure {
        void click();
    }

    public interface setCancle {
        void click();
    }

    public static final boolean isMobileNo(String mobiles) {
        Pattern p = Pattern.compile("13\\d{9}|14[57]\\d{8}|15[012356789]\\d{8}|18[012356789]\\d{8}|17[0678]\\d{8}");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }


    /*
    * 获取当前程序的版本号
    */
    public static String getVersion() {
        //获取packagemanager的实例
        PackageManager packageManager = BaseApplication.getContext().getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(BaseApplication.getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return packInfo.versionName;
    }

    /**
     * 加载当前时间。
     * 1.同一年的显示格式 05-11  07:45
     * 2.前一年或者更多格式 2015-11-12
     *
     * @param old
     * @return 需要显示的处理结果
     */
    public static String loadTime(String old) {
        String old_year = old.substring(0, 4);//获得old里面的年
        String now_year = new SimpleDateFormat("yyyy").format(new Date()).substring(0, 4);//获得当前的年
        if (old_year.equals(now_year)) {//两者为同一年
            return old.substring(5, 16);
        } else {
            return old.substring(0, 10);
        }
    }

    /**
     * 检查网络连接状态
     *
     * @return 连接成功
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) BaseApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static Bitmap getimage(Context contxt, String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        Bitmap header = BitmapFactory.decodeResource(contxt.getResources(), R.mipmap.main_zhengjian);

        float hh = header.getHeight();//这里设置高度为800f
        float ww = header.getWidth();//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     *                                                                       
     *    * @param bitmap      原图
     *    * @param edgeLength  希望得到的正方形部分的边长
     *    * @return  缩放截取正中部分后的位图。
     *    
     */

    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }
            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;
            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }
        return result;
    }

    /*自定义加载条*/
    public static Dialog ProgressDialog(Context context, Dialog progressDialog, String val, boolean istrue) {
        progressDialog = new Dialog(context, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(istrue);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText(val);
        return progressDialog;
    }

    /**
     * 获取当前应用的版本号：
     */
    public static String getVersionName() {
        // 获取packagemanager的实例
        String Version = "[Version:num]-[Registe:Mobile]";
        PackageManager packageManager = BaseApplication.getContext().getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(BaseApplication.getContext().getPackageName(), 0);
            String version = packInfo.versionName;
            return Version.replace("num", version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Version.replace("num", "1.0");
    }

    /**
     * 获得当前系统时间
     *
     * @return String类型的当前时间
     */
    public static String getNormalTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }

    /**
     * 获得当前时间 yyyy/MM/dd HH:mm:ss
     *
     * @return String类型的当前时间
     */
    public static String getNow() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }

    //将20160302210101转换为yyyy-MM-dd HH:mm:ss
    public static String DataTimeTO(String time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dfstr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

        Date date = null;
        try {
            date = df.parse(time);
            return dfstr.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 读取xml文件
     *
     * @param FileName 文件名
     * @return 文件内容
     */
    public static String getAssetsFileData(String FileName) {
        String str = "";
        try {
            InputStream is = BaseApplication.getContext().getAssets().open(FileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    //base64 string转换为bitmap
    public static Bitmap getBitmapByte(String str) {
        Bitmap bitmap = null;
        try {
            byte[] buffer = Base64.decode(str.getBytes(), Base64.DEFAULT);
            if (buffer != null && buffer.length > 0) {
                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获得屏幕高度宽度
     *
     * @return Point对象 point.x宽度。point.y高度
     */
    public static Point getScannerPoint() {
        WindowManager windowManager = (WindowManager) BaseApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getViewPoint(View view) {
        Point point = new Point();
        view.getDisplay().getSize(point);
        return point;
    }

    //获取当前时间的hhmmssfff
    public static String getQINGQIUMA() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts.toString());//yyyymmddhhmmssfff
        String str = ts.toString().replace(":", "").replace(".", "").replace("-", "").replace(" ", "");
        if (str.length() < 16) {
            str = str.substring(0);
        } else if (str.length() < 17) {
            str = str.substring(1);
        } else {
            str = str.substring(2);
        }
        return str;
    }

    /**
     * 比较时间的大小str1小返回true
     *
     * @param str1   起始时间
     * @param str2   结束时间
     * @param islong true,长时间串
     * @return
     */
    public static boolean DateCompare(String str1, String str2, boolean islong) {
        java.text.DateFormat df;
        if (islong) {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            df = new SimpleDateFormat("yyyy-MM-dd");
        }
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(df.parse(str1));
            c2.setTime(df.parse(str2));
        } catch (ParseException e) {
            System.err.println("格式不正确");
            return false;
        }
        int result = c1.compareTo(c2);
        if (result == 0) {
            //System.out.println("c1相等c2");
            return true;
        } else if (result >= 0) {
            return false;
            //System.out.println("c1小于c2");
        } else {
            // System.out.println("c1大于c2");
            return true;
        }
    }


    public static String getAssetsFileData(Context context, String FileName) {
        String str = "";
        try {
            InputStream is = context.getAssets().open(FileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 对URL进行编码操作
     *
     * @param text
     * @return
     */
    public static String URLEncodeImage(String text) {
        if (Utils.isEmptyString(text))
            return "";

        return URLEncoder.encode(text);
    }

    /**
     * 判断字符串是否为空,为空返回空串
     * http://bbs.3gstdy.com
     *
     * @param text
     * @return
     */
    public static String URLEncode(String text) {
        if (isEmptyString(text))
            return "";
        if (text.equals("null"))
            return "";
        return text;
    }

    /**
     * 检查蓝牙设备
     * http://bbs.3gstdy.com
     *
     * @param context,requestcode
     * @return
     */
    public static boolean checkBluetooth(Activity context, int requestCode) {
        /*
         * Intent serverIntent = new Intent(context, DeviceListActivity.class);
		 * context.startActivity(serverIntent); return true;
		 */

        boolean result = true;
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (null != ba) {
            if (!ba.isEnabled()) {
                result = false;
                Intent intent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivityForResult(intent, requestCode);// 或者ba.enable();
                // //同样的关闭WIFi为ba.disable();
            }
        }
        return result;
    }

    /**
     * 判断字符串是否为空
     * http://bbs.3gstdy.com
     *
     * @param str
     * @return
     */
    public static boolean isEmptyString(String str) {
        return (str == null || str.length() == 0);
    }

    /**
     * 将图片bitmap转换为base64字符串
     * http://bbs.3gstdy.com
     *
     * @param bitmap
     * @return 根据url读取出的图片的Bitmap信息
     */
    public static String encodeBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    .trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static final String allChar = "0123456789";

    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomChar(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return sb.toString();
    }


    /**
     * 获取系统的当前日期，格式为YYYYMMDD
     */
    public static String getSystemNowDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthOfYear = calendar.get(Calendar.MONTH) + 1;
        String monthStr = String.valueOf(monthOfYear);
        if (monthStr.length() < 2) {
            monthStr = "0" + monthStr;
        }
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(dayOfMonth);
        if (dayStr.length() < 2) {
            dayStr = "0" + dayStr;
        }
        return String.valueOf(year) + monthStr + dayStr;
    }

    /**
     * 带参数的跳页
     *
     * @param cla      需要跳转到的页面
     * @param listener 传参的接口
     */
    public static void IntentPost(Class cla, putListener listener) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setClass(BaseApplication.getContext(), cla);
        if (listener != null) {
            listener.put(intent);
        }
        BaseApplication.getContext().startActivity(intent);
    }

    /**
     * 不带参数的跳页
     *
     * @param cla 需要跳转到的页面
     */
    public static void IntentPost(Class cla) {
        IntentPost(cla, null);
    }

    /**
     * 判断ip地址是否符合格式（10.0.3.2）
     *
     * @param ip 需要检测的ip地址
     * @return 是否符合规定，true为符合。
     */
    public static boolean checkIP(String ip) {
        if (Utils.getContainSize(ip, ".") == 3 && ip.length() >= 7) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得key在val中存在的个数
     *
     * @param val 字符串
     * @param key 包含在key中的某字符
     * @return 存在的个数
     */
    public static int getContainSize(String val, String key) {
        if (val.contains(key)) {
            int length = val.length() - val.replace(key, "").length();
            if (length > 0) {
                return length;
            }
        }
        return 0;
    }

    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url
     * @return 根据url读取出的图片的Bitmap信息
     */
    public static Bitmap getBitmapByFile(String url) {
        if (url != "" && url != null) {
            try {
                FileInputStream fis = new FileInputStream(url);
                return BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param bitmap     原图
     * @param edgeLength 希望得到的正方形部分的边长
     * @return 缩放截取正中部分后的位图。
     */
    public static Bitmap centerImageBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }
            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;
            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }
        return result;
    }

    /**
     * 截取指定字符串并添加并在后面添加...
     *
     * @param val    截取前的字符串
     * @param length 截取字符长度
     * @return 处理之后的结果
     */
    public static String cutStringToDian(String val, int length) {
        if (val.length() >= length) {
            return val.substring(0, length) + "...";
        } else {
            return val;
        }
    }

    //得到手机的imei
    public static String getImei() {
        return ((TelephonyManager) BaseApplication.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    /**
     * 获得当前手机的手机号码
     *
     * @return
     */
    public static String getPhoneNum() {
        TelephonyManager phoneMgr = (TelephonyManager) BaseApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return phoneMgr.getLine1Number();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 跳页传参的接口
     */
    public interface putListener {
        void put(Intent intent);
    }
}
