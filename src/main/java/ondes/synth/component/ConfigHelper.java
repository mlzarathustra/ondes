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


    @SuppressWarnings("rawtypes")
    public static Map getMap(Map config, String inputName) {
        Object objInp =config.get(inputName);
        if (objInp != null && !( objInp instanceof Map)) {
            err.println(inputName+" must be a Map!");
            return null;
        }
        return (Map)objInp;
    }


    /**
     * We assume the two properties are "amp" and "out"
     *
     * @param config - the configuration map for this component
     * @param outputName - the name of the input (with 2 properties,
     *                     "amp" and "out")
     * @return - two objects: Integer and String, containing the
     *      values of the amp: and out: properties respectively.
     */
    public static Object[] getOutAmpPair(Map config, String outputName) {
        Map inputParams = getMap(config, outputName);
        if (inputParams == null) return null; // not necessarily an error

        Integer intInp = getInt(inputParams.get("amp"),
            "'amp' must be an integer.");

        String outStr = (String)inputParams.get("out");
        if (outStr == null) {
            err.println("out: property missing for output "+outputName);
        }
        if (intInp == null || outStr == null) return null;
        return new Object[] { intInp, outStr };
    }



    /**
     * <p>
     *     Inputs are often defined by a pair of numbers,
     *     "amp" and something else (like "semitones")
     * </p>
     * <p>
     *     TODO - return a range for the "inputProp" e.g. -100, 100
     * </p>
     *
     * @return an Integer and a Float, or null if the
     * input map is null.
     */
    @SuppressWarnings("rawtypes")
    public static Object[] getInAmpPair(Map config, String inputName, String inputProp) {
        Map inputParams = getMap(config, inputName);
        if (inputParams == null) return null; // not necessarily an error

        Integer intInp = getInt(inputParams.get("amp"),
            "'amp' must be an integer.");

        Float fltInp = getFloat(inputParams.get(inputProp),
            inputProp+" must be a decimal number.");

        if (intInp == null || fltInp == null) return null;
        return new Object[] { intInp, fltInp };
    }





}
