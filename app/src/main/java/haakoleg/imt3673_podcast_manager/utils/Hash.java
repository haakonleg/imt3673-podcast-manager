package haakoleg.imt3673_podcast_manager.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    /**
     * Calculates the MD5 hash as integer from a given array of strings
     * @param strings Array of strings to encode
     * @return The calculated MD5 hash encoded as integer value
     */
    public static int getHash(String... strings) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            for (String string : strings) {
                md.update(string.getBytes());
            }

            byte[] digest = md.digest();
            return ByteBuffer.wrap(digest).getInt();
        } catch (NoSuchAlgorithmException e) {
            Log.e(Hash.class.getName(), Log.getStackTraceString(e));
        }
        return -1;
    }
}