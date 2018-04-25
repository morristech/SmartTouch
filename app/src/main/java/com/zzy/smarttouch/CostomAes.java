package com.zzy.smarttouch;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CostomAes
{
	public static String encrypt(String sKey, String sData)
	{
		try
		{
			byte[] key = sKey.getBytes("UTF8");
			byte[] data = sData.getBytes("UTF8");
			
			byte[] result = encrypt(key,data);
			return HexFromBytes(result);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String sKey, String sData)
	{
		try
		{
			byte[] key = sKey.getBytes("UTF8");
			byte[] data = toByte(sData);

			byte[] result = decrypt(key, data);
			return new String(result,"UTF-8");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
		
	private static byte[] encrypt(byte[] key, byte[] data)
	{
		SecretKeySpec skeySpec;
		byte[] encrypted = null;
		try
		{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
			KeyGenerator kgen= KeyGenerator.getInstance("AES");
			skeySpec = new SecretKeySpec(key,kgen.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE,skeySpec);
			encrypted = cipher.doFinal(data);
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} 
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} 
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return encrypted;
	}

	private static byte[] decrypt(byte key[], byte[] data)
	{
		SecretKeySpec skeySpec;
		byte[] decrypted = null;
		try
		{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); 
			KeyGenerator kgen= KeyGenerator.getInstance("AES");
			skeySpec = new SecretKeySpec(key,kgen.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decrypted = cipher.doFinal(data);
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} 
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} 
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
		return decrypted;
	}

	public static byte[] toByte(String hexString)
	{
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
		{
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),16).byteValue();
		}
		return result;
	}
	
	private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8','9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String HexFromBytes(byte[] bytes) 
	{
		char[] hexChars = new char[bytes.length * 2];
		int j = 0;
		for (int i = 0; i < bytes.length; i++) 
		{
			int v = bytes[i] & 0xFF;
			hexChars[(j++)] = hexArray[(v >> 4)];
			hexChars[(j++)] = hexArray[(v & 0xF)];
		}
		return new String(hexChars);
	}
}
