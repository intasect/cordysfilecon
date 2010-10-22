/**
 * © 2005 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */

package com.cordys.coe.ac.fileconnector.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date/time utility methods.
 *
 * @author  mpoyhone
 */
public class DateTimeUtils
{
    /**
     * Regexp pattern to parse SOAP date time.
     */
    private static final Pattern pSoapDateTime = Pattern.compile("([-]?\\d{4,})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)?");
    /**
     * Group number for year.
     */
    private static final int DATETIME_YEAR = 1;
    /**
     * Group number for month.
     */
    private static final int DATETIME_MONTH = 2;
    /**
     * Group number for day.
     */
    private static final int DATETIME_DAY = 3;
    /**
     * Group number for hour.
     */
    private static final int DATETIME_HOUR = 4;
    /**
     * Group number for minute.
     */
    private static final int DATETIME_MINUTE = 5;
    /**
     * Group number for seconds.
     */
    private static final int DATETIME_SECOND = 6;
    /**
     * Group number for milliseconds.
     */
    private static final int DATETIME_MILLISECOND = 7;
    /**
     * Group number for timezone.
     */
    private static final int DATETIME_TIMEZONE = 8;

    /**
     * Same as the other variant but this takes the number of milliseconds to be used from the date
     * format string. Minumum and maximum are set to the number of 'S' elements found from the
     * format string.
     *
     * @param   sDateTime  Datetime string to be converted.
     * @param   sFormat    Date format string to be used.
     *
     * @return  Converted datetime as string.
     */
    public static String fixSoapDateTimeMillis(String sDateTime, String sFormat)
    {
        int iDigits = 0;
        String sDigitStr = sFormat.replaceFirst(".*(\\.S+).*", "$1");

        if (sDigitStr.length() < sFormat.length())
        {
            // Regex matched.
            iDigits = sDigitStr.length() - 1;
        }

        return fixSoapDateTimeMillis(sDateTime, iDigits, iDigits);
    }

    /**
     * Limits the number of millisecond digits in a SOAP datetime string to the specified range.
     * This is needed to overcome limitations in Java date parser. This method is inteded to be used
     * before parsing the datetime with SimpleDateFormat.
     *
     * @param   sDateTime   Datetime string to be converted.
     * @param   iMinDigits  Minimum number of digits to be used.
     * @param   iMaxDigits  Maximum number of digits to be used.
     *
     * @return  Converted datetime as string.
     */
    public static String fixSoapDateTimeMillis(String sDateTime, int iMinDigits, int iMaxDigits)
    {
        Matcher mMatcher = pSoapDateTime.matcher(sDateTime.trim());

        if (!mMatcher.matches())
        {
            // Invalid datetime string, so just return the original.
            return sDateTime;
        }

        // Recreate the datetime string. This allows more precise control
        // for slight differences between SOAP datetime and Java SimpleDateFormat
        // formats. For example we might handle negative years diffently. Also
        // timezones are handled correctly this way.
        StringBuffer sbBuffer = new StringBuffer(30);

        sbBuffer.append(mMatcher.group(DATETIME_YEAR)).append("-");
        sbBuffer.append(mMatcher.group(DATETIME_MONTH)).append("-");
        sbBuffer.append(mMatcher.group(DATETIME_DAY)).append("T");
        sbBuffer.append(mMatcher.group(DATETIME_HOUR)).append(":");
        sbBuffer.append(mMatcher.group(DATETIME_MINUTE)).append(":");
        sbBuffer.append(mMatcher.group(DATETIME_SECOND));

        // Use a maximum of three digits.
        String sMillis = mMatcher.group(DATETIME_MILLISECOND);

        if (iMaxDigits > 0)
        {
            if (sMillis == null)
            {
                sMillis = ".";
            }

            while (sMillis.length() < (iMinDigits + 1))
            {
                sMillis += "0";
            }

            if ((sMillis.length() - 1) > iMaxDigits)
            {
                sMillis = sMillis.substring(0, iMaxDigits + 1);
            }

            if (sMillis.equals("."))
            {
                // No digits added, so use an empty string.
                sMillis = "";
            }
        }
        else
        {
            // No milliseconds specified so do not add anything.
            sMillis = "";
        }

        sbBuffer.append(sMillis);

        String sTimeZone = mMatcher.group(DATETIME_TIMEZONE);

        if (sTimeZone != null)
        {
            sbBuffer.append(sTimeZone);
        }

        return sbBuffer.toString();
    }
}
