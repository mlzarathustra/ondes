package ondes.synth.component;

import static java.lang.System.err;

public interface ConfigHelper {

    default Double getDouble(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Double.parseDouble(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }
    default Float getFloat(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Float.parseFloat(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }
    default Integer getInt(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Integer.parseInt(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }



}
