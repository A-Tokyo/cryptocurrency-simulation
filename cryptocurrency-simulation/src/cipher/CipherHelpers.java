package cipher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;

public class CipherHelpers {
	private static final int KEY_LENGTH = 2048;
	private static final String KEY_ALGORITHM = "RSA";

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException{
		final KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		kpg.initialize(KEY_LENGTH); 
		return kpg.generateKeyPair();
	}

	public static byte[] encrypt(String message, Key key) {
		byte[] cipherText = null;
		try {
			final Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	public static String decrypt(byte[] message, Key key) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
			// decrypt the text using the key
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new String(dectyptedText);
	}

	public static void main (String[] args) throws NoSuchAlgorithmException {
		KeyPair kp = generateKeyPair();
		
		System.out.println ("-----BEGIN PRIVATE KEY-----");
		System.out.println (Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded()));
		System.out.println ("-----END PRIVATE KEY-----");
		System.out.println ("-----BEGIN PUBLIC KEY-----");
		System.out.println (Base64.getMimeEncoder().encodeToString(kp.getPublic().getEncoded()));
		System.out.println ("-----END PUBLIC KEY-----");

		String message = "I love Claudia";
		byte[] messageEncrypted = encrypt(message, kp.getPrivate());
		System.out.println("-----BEGIN messageEncrypted----");
		System.out.println(messageEncrypted);
		System.out.println("-----END messageEncrypted----");
		System.out.println("-----BEGIN messageDecrypted----");
		System.out.println(decrypt(messageEncrypted, kp.getPublic()));
		System.out.println("-----END messageDecrypted----");
	}
}
