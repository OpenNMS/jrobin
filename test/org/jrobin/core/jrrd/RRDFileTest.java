package org.jrobin.core.jrrd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.jrobin.core.RrdException;
import org.junit.Test;

public class RRDFileTest {

	@Test
	public void testTooShortForHeader() throws IOException, RrdException {
		File tempFile = new File("target/test-too-short.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		outputStream.write(0); //Write just one byte
		outputStream.close();
		
		try {
			RRDFile rrdFile = new RRDFile(tempFile);
			rrdFile.close();
			Assert.fail("Expected an RrdException");
		} catch (RrdException e) {
			//Expected; file is too short
		}
	}
	
	@Test
	public void testNoFloatCookie() throws IOException, RrdException {
		File tempFile = new File("target/test-no-float-cookie.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		this.write32BitHeaderToVersion(outputStream);
		byte[] padding = new byte[24];
		outputStream.write(padding);
		outputStream.close();
		
		try {
			RRDFile rrdFile = new RRDFile(tempFile);
			rrdFile.close();
			Assert.fail("Expected an RrdException");
		} catch (RrdException e) {
			//No cookie
		}
	}

	@Test
	public void test32BitLittleEndianness() throws IOException, RrdException {
		File tempFile=new File("target/test-endianness.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write32BitLittleEndianHeaderToFloatCookie(outputStream);
		byte[] padding = new byte[24]; //Arbitrary number; we just need 24 bytes total
		outputStream.write(padding);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		Assert.assertFalse("Expected little endian", rrdFile.isBigEndian());
		Assert.assertEquals("Expected 4-byte alignment", 4, rrdFile.getAlignment());
	}

	@Test
	public void test32BitBigEndianness() throws IOException, RrdException {
		File tempFile=new File("target/test-endianness.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write32BitBigEndianHeaderToFloatCookie(outputStream);
		byte[] padding = new byte[24]; //Arbitrary number; we just need 24 bytes total
		outputStream.write(padding);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		Assert.assertTrue("Expected big endian", rrdFile.isBigEndian());
		Assert.assertEquals("Expected 4-byte alignment", 4, rrdFile.getAlignment());

	}

	@Test
	public void test64BitLittleEndianness() throws IOException, RrdException {
		File tempFile=new File("target/test-endianness.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write64BitLittleEndianHeaderToFloatCookie(outputStream);
		byte[] padding = new byte[24]; //Arbitrary number; we just need 24 bytes total
		outputStream.write(padding);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		Assert.assertFalse("Expected little endian", rrdFile.isBigEndian());
		Assert.assertEquals("Expected 8-byte alignment", 8, rrdFile.getAlignment());
	}

	@Test
	public void test64BitBigEndianness() throws IOException, RrdException {
		File tempFile=new File("target/test-endianness.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write64BitBigEndianHeaderToFloatCookie(outputStream);
		byte[] padding = new byte[24]; //Arbitrary number; we just need 24 bytes total
		outputStream.write(padding);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		Assert.assertTrue("Expected big endian", rrdFile.isBigEndian());
		Assert.assertEquals("Expected 8-byte alignment", 8, rrdFile.getAlignment());

	}
	
	@Test
	public void testReadInt32BitLittleEndian() throws IOException, RrdException {
		File tempFile=new File("target/test-read-int.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write32BitLittleEndianHeaderToFloatCookie(outputStream);

		//Write out 3 integers (the rest of the normal header), each 32 bits, in little endian format.
		byte int1[] = {0x12, 0x34, 0x56, 0x78}; //Gives the integer 0x78563412 in little endian
		byte int2[] = {0x78, 0x56, 0x34, 0x12}; //Gives the integer 0x12345678 in little endian
		byte int3[] = {0x34, 0x12, 0x78, 0x56}; //Gives the integer 0x56781234 in little endian

		outputStream.write(int1);
		outputStream.write(int2);
		outputStream.write(int3);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		rrdFile.skipBytes(20); //Skip the string cookie, version, padding, and float cookie
		Assert.assertEquals(0x78563412, rrdFile.readInt());
		Assert.assertEquals(0x12345678, rrdFile.readInt());
		Assert.assertEquals(0x56781234, rrdFile.readInt());
	}
	
	@Test
	public void testReadInt32BitBigEndian() throws IOException, RrdException {
		File tempFile=new File("target/test-read-int.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write32BitBigEndianHeaderToFloatCookie(outputStream);

		//Write out 3 integers (the rest of the normal header), each 32 bits, in little endian format.
		byte int1[] = {0x12, 0x34, 0x56, 0x78}; //Gives the integer 0x12345678 in little endian
		byte int2[] = {0x78, 0x56, 0x34, 0x12}; //Gives the integer 0x78563412 in little endian
		byte int3[] = {0x34, 0x12, 0x78, 0x56}; //Gives the integer 0x34127856 in little endian

		outputStream.write(int1);
		outputStream.write(int2);
		outputStream.write(int3);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		rrdFile.skipBytes(20); //Skip the string cookie, version, padding, and float cookie
		Assert.assertEquals(0x12345678, rrdFile.readInt());
		Assert.assertEquals(0x78563412, rrdFile.readInt());
		Assert.assertEquals(0x34127856, rrdFile.readInt());
	}
	
	@Test
	public void testReadInt64BitLittleEndian() throws IOException, RrdException {
		File tempFile=new File("target/test-read-int.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write64BitLittleEndianHeaderToFloatCookie(outputStream);

		//Write out 3 integers (the rest of the normal header), each 64 bits, in little endian format.
		//However, we're expecting only an int (32-bits) back, so the value we're expecting from a 
		// little-endian file is the first four bytes only.  The latter-four bytes are ignored.
		//We write them with real possibly mis-interpretable numbers though, to double check that it's reading correctly
		byte int1[] = {0x12, 0x34, 0x56, 0x78, 0x77, 0x66, 0x55, 0x44}; 
		byte int2[] = {0x78, 0x56, 0x34, 0x12, 0x77, 0x66, 0x55, 0x44}; 
		byte int3[] = {0x34, 0x12, 0x78, 0x56, 0x77, 0x66, 0x55, 0x44}; 

		outputStream.write(int1);
		outputStream.write(int2);
		outputStream.write(int3);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		rrdFile.skipBytes(24); //Skip the string cookie, version, padding, and float cookie
		Assert.assertEquals(0x78563412, rrdFile.readInt());
		Assert.assertEquals(0x12345678, rrdFile.readInt());
		Assert.assertEquals(0x56781234, rrdFile.readInt());
	}
	
	
	@Test
	public void testReadInt64BitBigEndian() throws IOException, RrdException {
		File tempFile=new File("target/test-read-int.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write64BitBigEndianHeaderToFloatCookie(outputStream);

		//Write out 3 integers (the rest of the normal header), each 64 bits, in little endian format.
		//However, we're expecting only an int (32-bits) back, so the value we're expecting from a 
		// big-endian file is the *last* four bytes only.  The first four bytes are ignored.
		//We write them with real possibly mis-interpretable numbers though, to double check 
		//that it's reading correctly
		byte int1[] = { 0x77, 0x66, 0x55, 0x44, 0x78, 0x56, 0x34, 0x12}; 
		byte int2[] = { 0x77, 0x66, 0x55, 0x44, 0x12, 0x34, 0x56, 0x78}; 
		byte int3[] = { 0x77, 0x66, 0x55, 0x44, 0x78, 0x12, 0x56, 0x34}; 

		outputStream.write(int1);
		outputStream.write(int2);
		outputStream.write(int3);
		outputStream.close();
		
		RRDFile rrdFile = new RRDFile(tempFile);
		rrdFile.skipBytes(24); //Skip the string cookie, version, padding, and float cookie
		Assert.assertEquals(0x78563412, rrdFile.readInt());
		Assert.assertEquals(0x12345678, rrdFile.readInt());
		Assert.assertEquals(0x78125634, rrdFile.readInt());
	}
	
	/*
	 * No need to test readDouble specifically; it's been tested by all the other test writing the
	 * float cookies and reading them back in the RRDFile constructor.
	 * If there's a problem, it'll show up there
	 */
	
	@Test
	public void testReadString() throws IOException, RrdException {
		File tempFile=new File("target/test-read-string.rrd");
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		write64BitLittleEndianHeaderToFloatCookie(outputStream); //Simplest way to get at least 24 bytes in the file
		outputStream.close();
		//The first 4 bytes of the file must be null terminated string "RRD" (Constants.COOKIE)
		//That's a good enough test
		RRDFile rrdFile = new RRDFile(tempFile);
		String cookie = rrdFile.readString(4);
		Assert.assertEquals(Constants.COOKIE, cookie);
	}
	
	/*Writes the header, up to the float cookie, for a 32 bit little endian file */
	private void write32BitLittleEndianHeaderToFloatCookie(FileOutputStream outputStream)
			throws FileNotFoundException, IOException {
		
		this.write32BitHeaderToVersion(outputStream);
		outputStream.write(Constants.FLOAT_COOKIE_LITTLE_ENDIAN);
	}
	
	/*Writes the header, up to the float cookie, for a 32 bit little endian file */
	private void write32BitBigEndianHeaderToFloatCookie(FileOutputStream outputStream)
			throws FileNotFoundException, IOException {
		
		this.write32BitHeaderToVersion(outputStream);
		outputStream.write(Constants.FLOAT_COOKIE_BIG_ENDIAN);
	}

	//The same for little or big endian (byte by byte text basically)
	//But, padding varies for 32 vs 64 bit
	private void write32BitHeaderToVersion(FileOutputStream outputStream)
	throws FileNotFoundException, IOException {

		outputStream.write(Constants.COOKIE.getBytes());
		outputStream.write(0); //Null terminate the string
		outputStream.write(Constants.VERSION3.getBytes());
		for(int i=0; i<4; i++) {
			outputStream.write(0); //Null terminate the string and add 3 null bytes to pad to 32-bits
		}
	}
	
	/*Writes the header, up to the float cookie, for a 32 bit little endian file */
	private void write64BitLittleEndianHeaderToFloatCookie(FileOutputStream outputStream)
			throws FileNotFoundException, IOException {
		
		this.write64BitHeaderToVersion(outputStream);
		outputStream.write(Constants.FLOAT_COOKIE_LITTLE_ENDIAN);
	}

	/*Writes the header, up to the float cookie, for a 32 bit little endian file */
	private void write64BitBigEndianHeaderToFloatCookie(FileOutputStream outputStream)
			throws FileNotFoundException, IOException {
		
		this.write64BitHeaderToVersion(outputStream);
		outputStream.write(Constants.FLOAT_COOKIE_BIG_ENDIAN);
	}

	//The same for little or big endian (byte by byte text basically)
	//But, padding varies for 32 vs 64 bit
	private void write64BitHeaderToVersion(FileOutputStream outputStream)
	throws FileNotFoundException, IOException {

		outputStream.write(Constants.COOKIE.getBytes());
		outputStream.write(0); //Null terminate the string
		outputStream.write(Constants.VERSION3.getBytes());
		for(int i=0; i<8; i++) {
			outputStream.write(0); //Null terminate the string and add 7 null bytes to pad to 64-bits
		}
	}

}
