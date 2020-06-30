package ondes.synth.component;

import java.util.Map;

import static java.lang.System.err;

public class ConfigHelper {

    public static Double getDouble(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Double.parseDouble(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }
    public static Float getFloat(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Float.parseFloat(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }
    public static Integer getInt(Object obj, String excMsg) {
        if (obj == null) return null;
            try {
                return Integer.parseInt(obj.toString());
            }
            catch (Exception ex) {
                err.println(excMsg);
                return null;
            }
    }

    /**
     * Inputs are often defined by a pair of numbers,
     * "amp" and something else (like "semitones")
     *
     * @return an Integer and a Float, or null if the
     * input map is null.
     */
    @SuppressWarnings("rawtypes")
    public static Object[] getAmpPair(Map config, String inputName, String inputProp) {
        Object objInp = config.get(inputName);
        if (objInp != null && !( objInp instanceof Map)) {
            err.println(inputName+" must be a Map!");
            return null;
        }
        if (objInp == null) return null; // not necessarily an error

        Map inputParams=(Map)objInp;
        Integer intInp = getInt(inputParams.get("amp"),
            "'amp' must be an integer.");

        Float fltInp = getFloat(inputParams.get(inputProp),
            inputProp+" must be a decimal number.");

        if (intInp == null || fltInp == null) return null;
        return new Object[] { intInp, fltInp };
    }





}
