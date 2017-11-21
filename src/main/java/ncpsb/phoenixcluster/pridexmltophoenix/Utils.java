package ncpsb.phoenixcluster.pridexmltophoenix;

/**
 * Created by baimi on 2017/11/10.
 */
public class Utils {
    public static String getShortFileName(String fileName) {
        return "";
    }

    public static String doubleArrayToString(double[] inputArray) {
        StringBuffer stringBuffer = new StringBuffer();
        for (double item : inputArray) {
            stringBuffer.append(item);
            stringBuffer.append(",");
        }
        stringBuffer.setLength(stringBuffer.length() - 1);

        return stringBuffer.toString();
    }
}
