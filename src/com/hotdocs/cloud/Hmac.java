/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * A utility class that calculates the HMAC for a
 * HotDocs Cloud Services request.
 */
public class Hmac {
    /**
     * Canonicalizes a collection of parameters into a string.
     * The canonicalization algorithm is as follows:
     * - Strings are included as-is, even if they contain '\n'.
     * - Integral types are converted to strings using base 10 and no leading zeros.
     * - Enums and bools are converted to strings.
     * - DateTime types are converted to UTC and formatted as "yyyy-MM-ddTHH:mm:ssZ".
     * - Map<String,String>s are sorted alphabetically by key, and projected to
     *   individual strings as "key=value", e.g. keyA=valueA\nkeyB=valueB\n etc.
     * - Nulls and all other types are converted to empty strings.
     * - All parameters are separated by '\n'.
     * 
     * @param params  Objects that are combined to form the canonicalized string
     * @return        The canonicalized string
     */
    public static String canonicalize(Iterable<Object> params) {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Build the string
        StringBuilder sb = new StringBuilder();
        for (Object p : params) {
            if (p instanceof String || p instanceof Integer) {
                sb.append(p.toString());
            } else if (p instanceof Boolean) {
                sb.append((Boolean)p ? "True" : "False");
            } else if (p instanceof Enum) {
                sb.append(((Enum<?>) p).name());
            } else if (p instanceof Date) {
                sb.append(dateFormat.format((Date) p));
            } else if (p instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked") // Casting Object to Map<String, String>
                Map<String, String> map = (Map<String, String>)p;
                if (!map.isEmpty()) {
                    ConcurrentSkipListMap<String, String> sortedMap =
                            new ConcurrentSkipListMap<String, String>(map);
                    for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
                        sb.append(entry.getKey());
                        sb.append('=');
                        sb.append(entry.getValue());
                        sb.append('\n');
                    }
                    continue;
                }
            }
            sb.append('\n');
        }

        // Remove trailing \n
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Creates a BASE64-encoded HMAC-SHA1 hash from a
     * collection of parameters and a security token.
     * 
     * @param signingKey  The subscriber's unique signing key
     * @param params      The parameters to be hashed
     * @return            The BASE64-encoded HMAC
     * @throws HmacException 
     */
    public static String calculateHmac(String signingKey, Iterable<Object> params)
            throws HmacException {
        try {
            SecretKeySpec key = new SecretKeySpec(signingKey.getBytes("UTF-8"), "HmacSHA1");
    
            String stringToSign = canonicalize(params);
            byte[] bytesToSign = stringToSign.getBytes("UTF-8");
    
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] signature = mac.doFinal(bytesToSign);

            return DatatypeConverter.printBase64Binary(signature);
            
        } catch (UnsupportedEncodingException|NoSuchAlgorithmException|InvalidKeyException ex) {
            throw new HmacException(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
