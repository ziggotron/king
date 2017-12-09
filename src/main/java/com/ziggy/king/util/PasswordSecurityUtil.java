package com.ziggy.king.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

public class PasswordSecurityUtil {

	public String createSalt() {
		String salt = null;
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			byte[] bSalt = new byte[8];
			random.nextBytes(bSalt);
			salt = byteToBase64(bSalt);
		} catch (NoSuchAlgorithmException nae) {
			nae.printStackTrace();
		}
		return salt;
	}
	
	public String hashPassword(String passwordPlain, String salt) {
		String passwordHash = null;
		try {
			byte[] bSalt = base64ToByte(salt);
			byte[] bDigest = getHash(passwordPlain, bSalt);
			passwordHash = byteToBase64(bDigest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return passwordHash;
	}

	public byte[] getHash(String password, byte[] salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.reset();
			digest.update(salt);
			byte[] input = digest.digest(password.getBytes("UTF-8"));
			digest.reset();
			input = digest.digest(input);
			return input;
		} catch (NoSuchAlgorithmException nae) {
			nae.printStackTrace();
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return null;
	}

	public Boolean login(String storedPassword, String storedSalt, String passwordPlain) {
		try {
			byte[] bDigest = base64ToByte(storedPassword);
			byte[] bSalt = base64ToByte(storedSalt);
			byte[] proposedDigest = getHash(new String(passwordPlain), bSalt);
			if (Arrays.equals(proposedDigest, bDigest)) {
				return true;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}
	
	public String generatePassword() {
		SecureRandom sr = new SecureRandom();
		return new BigInteger(64, sr).toString(32);
	}

	public static byte[] base64ToByte(String data) throws IOException {
		return Base64.decodeBase64(data);
	}

	public static String byteToBase64(byte[] data) {
		return Base64.encodeBase64String(data);
	}
}
